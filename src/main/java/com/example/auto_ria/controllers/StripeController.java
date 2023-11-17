package com.example.auto_ria.controllers;

import com.example.auto_ria.dao.PremiumPlanDaoSQL;
import com.example.auto_ria.dao.UserDaoSQL;
import com.example.auto_ria.enums.EAccountType;
import com.example.auto_ria.enums.EMail;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.mail.FMService;
import com.example.auto_ria.models.SellerSQL;
import com.example.auto_ria.models.premium.PremiumPlan;
import com.example.auto_ria.models.requests.SetPaymentSourceRequest;
import com.example.auto_ria.services.CommonService;
import com.example.auto_ria.services.StripeService;
import com.example.auto_ria.services.UsersServiceMySQLImpl;
import com.stripe.Stripe;
import com.stripe.model.Customer;
import com.stripe.param.CustomerCreateParams;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping(value = "payments")
public class StripeController {

    private UsersServiceMySQLImpl usersServiceMySQL;
    private CommonService commonService;
    private Environment environment;
    private PremiumPlanDaoSQL premiumPlanDaoSQL;
    private UserDaoSQL userDaoSQL;
    private FMService mailer;
    private StripeService stripeService;

    @SneakyThrows
    @PostMapping("/webhooks/stripe")
    public void handleInvoicePaymentFailedWebhook(@RequestBody Map<String, Object> event) {
        String type = (String) event.get("type");

        System.out.println(type); //todo listen to this!!
        System.out.println("type");

        if (type.equals("invoice.payment_failed")) {
            Map<String, Object> eventData = (Map<String, Object>) event.get("data");
            Map<String, Object> objectData = (Map<String, Object>) eventData.get("object");

            String customerId = (String) objectData.get("customer");
            String customerEmail = (String) objectData.get("customer_email");
            String customerName = (String) objectData.get("customer_name");

            PremiumPlan premiumPlan = premiumPlanDaoSQL.findByCustomerId(customerId);
            SellerSQL owner = userDaoSQL.findByPaymentSource(customerId);

            premiumPlan.setActive(false);
            premiumPlanDaoSQL.save(premiumPlan);

            owner.setAccountType(EAccountType.BASIC);
            userDaoSQL.save(owner);

            Map<String, Object> args = new HashMap<>();
            args.put("url", "http://localhost:3000");
            args.put("name", "customer name"); //todo real params

            mailer.sendEmail("haisicraisi@gmail.com", EMail.PAYMENT_FAILED, args);

        }

    }

    @SneakyThrows
    @PostMapping("/add-payment-source")
    public ResponseEntity<String> addPaymentSource(
            @RequestBody SetPaymentSourceRequest body
    ) {
        try {
            System.out.println(body.getToken());
            Stripe.apiKey = environment.getProperty("Stripe.ApiKey");

            SellerSQL sellerSQL = usersServiceMySQL.getById(body.getId()).getBody();

            boolean sourcePresent = sellerSQL.isPaymentSourcePresent();
            String paymentToken = body.getToken();

//todo add path to security
            if (!sourcePresent) {
                Customer customer = Customer.create(
                        CustomerCreateParams.builder()
                                .setName(sellerSQL.getName() + sellerSQL.getLastName())
                                .setEmail(sellerSQL.getEmail())
                                .setSource(paymentToken)
                                .build()
                );

                sellerSQL.setPaymentSource(customer.getId());
                sellerSQL.setPaymentSourcePresent(true);
                userDaoSQL.save(sellerSQL);

            } else {
                String paymentSource = sellerSQL.getPaymentSource();
                Customer stripeCustomer = Customer.retrieve(paymentSource);

                Map<String, Object> params = new HashMap<>();
                params.put("source", paymentToken);
                stripeCustomer.update(params);
            }

            return ResponseEntity.ok("Card attached successfully");
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @SneakyThrows
    @PostMapping("/buy-premium")
    public ResponseEntity<String> getPremium(
            @RequestBody SetPaymentSourceRequest body,
            HttpServletRequest request
    ) {

        try {
            SellerSQL sellerSQL = commonService.extractSellerFromHeader(request);
//            SellerSQL sellerSQL = usersServiceMySQL.getById(body.getId()).getBody();

            if (sellerSQL.getAccountType().equals(EAccountType.PREMIUM)) {
                throw new CustomException("Premium account is already bought", HttpStatus.BAD_REQUEST);
            }

            stripeService.createPayment(body, sellerSQL);

            sellerSQL.setAccountType(EAccountType.PREMIUM);
            userDaoSQL.save(sellerSQL);
            return ResponseEntity.ok("Premium bought successfully");
        } catch (CustomException e) {
            System.out.println(e.getMessage());
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @SneakyThrows
    @PostMapping("/cancel-subscription")
    public ResponseEntity<String> cancel(
            @RequestBody SetPaymentSourceRequest body,
            HttpServletRequest request
    ) {

        try {
            SellerSQL sellerSQL = commonService.extractSellerFromHeader(request);
//            SellerSQL sellerSQL = usersServiceMySQL.getById(body.getId()).getBody();

            if (!sellerSQL.getAccountType().equals(EAccountType.PREMIUM)) {
                throw new CustomException("Account with no subscription", HttpStatus.BAD_REQUEST);
            }

            PremiumPlan premiumPlan = premiumPlanDaoSQL.findBySellerId(sellerSQL.getId());

            stripeService.cancelSubscription(premiumPlan);

            return ResponseEntity.ok("Subscription canceled. " +
                    "Current subscription's expiry date: " + premiumPlan.getEndDate());
        } catch (CustomException e) {
            System.out.println(e.getMessage());
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

}
