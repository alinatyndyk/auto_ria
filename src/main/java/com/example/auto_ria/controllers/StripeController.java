package com.example.auto_ria.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.auto_ria.dao.premium.PremiumPlanDaoSQL;
import com.example.auto_ria.dao.user.UserDaoSQL;
import com.example.auto_ria.enums.EAccountType;
import com.example.auto_ria.enums.EMail;
import com.example.auto_ria.enums.ETokenRole;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.mail.FMService;
import com.example.auto_ria.models.premium.PremiumPlan;
import com.example.auto_ria.models.requests.SetPaymentSourceRequest;
import com.example.auto_ria.models.user.UserSQL;
import com.example.auto_ria.services.CommonService;
import com.example.auto_ria.services.otherApi.StripeService;
import com.example.auto_ria.services.user.UsersServiceMySQLImpl;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.param.CustomerCreateParams;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping(value = "payments")
@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)

public class StripeController {

    private CommonService commonService;
    private Environment environment;
    private PremiumPlanDaoSQL premiumPlanDaoSQL;
    private UserDaoSQL userDaoSQL;
    private FMService mailer;
    private StripeService stripeService;
    private UsersServiceMySQLImpl usersServiceMySQL;

    @SuppressWarnings("unchecked")
    @PostMapping("/webhooks/stripe")
    public void handleInvoicePaymentFailedWebhook(@RequestBody Map<String, Object> event) {
        String type = (String) event.get("type");

        if (type.equals("invoice.payment_failed")) {
            Map<String, Object> eventData = (Map<String, Object>) event.get("data");
            Map<String, Object> objectData = (Map<String, Object>) eventData.get("object");

            String customerId = (String) objectData.get("customer");

            PremiumPlan premiumPlan = premiumPlanDaoSQL.findByCustomerId(customerId);
            UserSQL owner = userDaoSQL.findByPaymentSource(customerId);

            premiumPlan.setActive(false);
            premiumPlanDaoSQL.save(premiumPlan);

            owner.setAccountType(EAccountType.BASIC);
            userDaoSQL.save(owner);

            Map<String, Object> args = new HashMap<>();
            args.put("url", "http://localhost:3000");
            args.put("name", owner.getName() + owner.getLastName());

            mailer.sendEmail(owner.getEmail(), EMail.PAYMENT_FAILED, args);

        }

    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/add-payment-source")
    public ResponseEntity<String> addPaymentSource(
            @RequestBody SetPaymentSourceRequest body,
            HttpServletRequest request) {
        try {
            UserSQL userSQL;

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                userSQL = usersServiceMySQL.getByEmail(userDetails.getUsername());
                if (userSQL == null) {
                    throw new CustomException("Invalid token", HttpStatus.FORBIDDEN);
                }
            } else {
                throw new CustomException("Unauthorized", HttpStatus.UNAUTHORIZED);
            }

            Stripe.apiKey = environment.getProperty("Stripe.ApiKey");

            boolean sourcePresent = userSQL.isPaymentSourcePresent();
            String paymentToken = body.getToken();

            if (!sourcePresent) {
                try {

                    Customer customer = Customer.create(
                            CustomerCreateParams.builder()
                                    .setName(userSQL.getName() + userSQL.getLastName())
                                    .setEmail(userSQL.getEmail())
                                    .setSource(paymentToken)
                                    .build());
                    userSQL.setPaymentSource(customer.getId());
                    userSQL.setPaymentSourcePresent(true);
                    userDaoSQL.save(userSQL);
                } catch (StripeException e) {
                    throw new CustomException("Couldnt create payment", HttpStatus.EXPECTATION_FAILED);
                }

            } else {
                String paymentSource = userSQL.getPaymentSource();

                try {

                    Customer stripeCustomer = Customer.retrieve(paymentSource);

                    Map<String, Object> params = new HashMap<>();
                    params.put("source", paymentToken);
                    stripeCustomer.update(params);
                } catch (StripeException e) {
                    throw new CustomException("Couldnt access payment info", HttpStatus.EXPECTATION_FAILED);

                }
            }

            return ResponseEntity.ok("Card attached successfully");
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/buy-premium")
    public ResponseEntity<String> getPremium(
            @RequestBody SetPaymentSourceRequest body,
            HttpServletRequest request) {
        try {

            Stripe.apiKey = environment.getProperty("Stripe.ApiKey");
            String email = commonService.extractEmailFromHeader(request, ETokenRole.USER);

            UserSQL sellerSQL = userDaoSQL.findUserByEmail(email);

            if (sellerSQL == null) {
                throw new CustomException("Invalid token", HttpStatus.BAD_REQUEST);
            } else if (sellerSQL.getAccountType().equals(EAccountType.PREMIUM)) {
                throw new CustomException("Premium account is already bought", HttpStatus.BAD_REQUEST);
            }

            stripeService.createPayment(body, sellerSQL);

            sellerSQL.setAccountType(EAccountType.PREMIUM);
            userDaoSQL.save(sellerSQL);
            return ResponseEntity.ok("Premium bought successfully");
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/cancel-subscription")
    public ResponseEntity<String> cancel(
            HttpServletRequest request) {

        try {
            UserSQL sellerSQL = commonService.extractUserFromHeader(request);

            if (!sellerSQL.getAccountType().equals(EAccountType.PREMIUM)) {
                throw new CustomException("Account with no subscription", HttpStatus.BAD_REQUEST);
            }

            PremiumPlan premiumPlan = premiumPlanDaoSQL.findByUserId(sellerSQL.getId());

            stripeService.cancelSubscription(premiumPlan);

            return ResponseEntity.ok("Subscription canceled. " +
                    "Current subscription's expiry date: " + premiumPlan.getEndDate());
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

}
