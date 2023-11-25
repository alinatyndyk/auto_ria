package com.example.auto_ria.services.otherApi;

import com.example.auto_ria.dao.premium.PremiumPlanDaoSQL;
import com.example.auto_ria.enums.EAccountType;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.models.user.SellerSQL;
import com.example.auto_ria.models.premium.PremiumPlan;
import com.example.auto_ria.models.requests.SetPaymentSourceRequest;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.param.*;
import lombok.AllArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@AllArgsConstructor
public class StripeService {

    private Environment environment;

    private PremiumPlanDaoSQL premiumPlanDaoSQL;

    public void createPayment(SetPaymentSourceRequest body, SellerSQL sellerSQL) {
        try {

            Stripe.apiKey = environment.getProperty("Stripe.ApiKey");

            String defaultSource;

            String stripeId = sellerSQL.getPaymentSource();
            boolean stripePresent = sellerSQL.isPaymentSourcePresent();
            if (stripeId == null) {
                defaultSource = null;
            } else {
                Customer payer = Customer.retrieve(stripeId);
                defaultSource = payer.getInvoiceSettings().getDefaultPaymentMethod();
            }

            String paymentToken;
            String customerId;

            if (body.isUseDefaultCard() && body.isSetAsDefaultCard()) {
                throw new CustomException(
                        "The mentioned source would be already defined as default source", HttpStatus.BAD_REQUEST);
            }

            if (body.isUseDefaultCard() && defaultSource == null) {
                throw new CustomException("No default source is present. " +
                        "Attach a card to your account to make your payments faster. Visit http*******", HttpStatus.BAD_REQUEST);
            }

            if (body.isSetAsDefaultCard() && defaultSource != null) {
                throw new CustomException("Default source is already defined. " +
                        "You can change it at any moment at - Http//3000/attach-card", HttpStatus.BAD_REQUEST);
            }

            if (body.isAutoPay() && !body.isSetAsDefaultCard()) {
                throw new CustomException("Subscription requires a default card", HttpStatus.BAD_REQUEST);
            }

            try {
                if (stripePresent && defaultSource != null && body.isUseDefaultCard()) {
                    System.out.println("first");
                    paymentToken = defaultSource;
                    customerId = stripeId;
                } else if (stripePresent && !body.isUseDefaultCard() && !body.isSetAsDefaultCard()) { //todo
                    System.out.println("first1");
                    paymentToken = body.getToken();
                    customerId = stripeId;

                } else if (stripePresent && defaultSource == null && body.isSetAsDefaultCard()) {
                    System.out.println("first2");
                    Customer stripeCustomer = Customer.retrieve(stripeId);

                    //todo check if adds
                    Map<String, Object> params = new HashMap<>();
                    params.put("source", body.getToken());

                    stripeCustomer.update(params);

                    paymentToken = body.getToken();
                    customerId = stripeId;

                } else if (!stripePresent && !body.isSetAsDefaultCard()) {
                    System.out.println("first3");
                    Customer customer = Customer.create(
                            CustomerCreateParams.builder()
                                    .setName(sellerSQL.getName() + sellerSQL.getLastName())
                                    .setEmail(sellerSQL.getEmail())
                                    .setSource(body.getToken())
                                    .build()
                    );
                    paymentToken = body.getToken();
                    customerId = customer.getId();
                    System.out.println(customerId + " customerID");
                } else if (!stripePresent && body.isSetAsDefaultCard()) {
                    System.out.println("first4");
                    Customer customer = Customer.create(
                            CustomerCreateParams.builder()
                                    .setName(sellerSQL.getName() + " hello " + sellerSQL.getLastName())
                                    .setEmail(sellerSQL.getEmail())
                                    .setSource(body.getToken())
                                    .build()
                    );
                    paymentToken = body.getToken();
                    customerId = customer.getId();
                } else {
                    throw new CustomException("Payment params invalid", HttpStatus.BAD_REQUEST);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                throw new CustomException("Please check your parameters" + e.getMessage(), HttpStatus.CONFLICT);

            }

            if (paymentToken != null && customerId != null) {
                System.out.println("inside");
                if (body.isAutoPay()) {

                    System.out.println("inside2 auto pay");
                    Price price = Price.retrieve("price_1OCqatAe4RILjJWGmXhBnHeX");

                    System.out.println(price + "price");
                    try {
                        Subscription subscription = Subscription.create(SubscriptionCreateParams.builder()
                                .setCustomer(customerId)
                                .addItem(SubscriptionCreateParams.Item.builder().setPrice(price.getId()).build())
                                .build());
                    System.out.println(subscription);
                    System.out.println("subscription----------------------------");
                    }catch (Exception e){
                        throw new CustomException(e.getMessage() + " message", HttpStatus.BAD_REQUEST);
                    }

                    //todo cron delete long expired subscriptions

                    PremiumPlan premiumPlan = premiumPlanDaoSQL.findBySellerId(sellerSQL.getId());

                    if (premiumPlan != null) {
                        premiumPlan.setAutoPayments(body.isAutoPay());
                        premiumPlan.setStartDate(LocalDate.now());
                        premiumPlan.setEndDate(LocalDate.now().plusMonths(1));
//                        premiumPlan.setSubId(subscription.getId()); todo renew
                        premiumPlan.setActive(true); //todo save

                    } else {
                        premiumPlanDaoSQL.save(PremiumPlan.builder()
                                .autoPayments(body.isAutoPay())
                                .startDate(LocalDate.now())
                                .endDate(LocalDate.now().plusMonths(1))
                                .sellerId(sellerSQL.getId())
                                .customerId(customerId)
//                                .subId(subscription.getId()) renew
                                .isActive(true)
                                .build());
                    }

                    sellerSQL.setAccountType(EAccountType.PREMIUM);

                } else {
                    Price price = Price.retrieve("price_1OCqatAe4RILjJWGmXhBnHeX");

                    System.out.println("else");

                    System.out.println(paymentToken + " payment token");

                    Map<String, Object> paymentMethodParams = new HashMap<>();
                    paymentMethodParams.put("type", "card");
//                    paymentMethodParams.put("card", Collections.singletonMap("token", paymentToken));
                    paymentMethodParams.put("card", Collections.singletonMap("token", "tok_visa")); //problem with token
                    PaymentMethod paymentMethod = PaymentMethod.create(paymentMethodParams);
                    System.out.println(paymentMethod + "payment method");

                    PaymentIntentCreateParams createParams = new PaymentIntentCreateParams.Builder()
                            .setAmount(price.getUnitAmount())
                            .setCurrency("usd")
                            .setDescription("Premium 1m bought")
                            .setPaymentMethod(paymentMethod.getId())
//                            .setCustomer(customerId)
                            .setConfirm(true)
                            .build();
                    System.out.println(createParams + "payment params");

                    PaymentIntent.create(createParams);
                    System.out.println("created");

                    PremiumPlan premiumPlan = premiumPlanDaoSQL.findBySellerId(sellerSQL.getId());
                    if (premiumPlan != null) {
                        premiumPlan.setAutoPayments(body.isAutoPay());
                        premiumPlan.setStartDate(LocalDate.now());
                        premiumPlan.setEndDate(LocalDate.now().plusMonths(1));
                        premiumPlan.setSubId(null);
                        premiumPlan.setActive(true);
                    } else {
                        premiumPlanDaoSQL.save(PremiumPlan.builder()
                                .autoPayments(body.isAutoPay())
                                .startDate(LocalDate.now())
                                .endDate(LocalDate.now().plusMonths(1))
                                .sellerId(sellerSQL.getId())
                                .customerId(customerId)
                                .subId(null)
                                .isActive(true)
                                .build());
                    }

                    sellerSQL.setAccountType(EAccountType.PREMIUM); //todo
                }


            } else {
                throw new CustomException("Source attachment fail: credential provided is null or invalid", HttpStatus.BAD_REQUEST);
            }
        } catch (StripeException e) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
            return;
        }
        ResponseEntity.ok(Map.of("message", "Payment completed successfully"));
    }

    public void cancelSubscription(PremiumPlan premiumPlan) {
        try {
            Stripe.apiKey = environment.getProperty("Stripe.ApiKey");

            Customer customer = Customer.retrieve(premiumPlan.getCustomerId());
            Subscription subscription = customer.getSubscriptions().getData().get(0);

            SubscriptionUpdateParams params = new SubscriptionUpdateParams.Builder()
                    .setCancelAt(premiumPlan.getEndDate().toEpochDay())
                    .build();

            subscription.update(params);

        } catch (Exception e) {
            throw new CustomException("Could not cancel subscription", HttpStatus.EXPECTATION_FAILED);
        }
    }
}