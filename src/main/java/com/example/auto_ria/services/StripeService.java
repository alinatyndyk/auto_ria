package com.example.auto_ria.services;

import com.example.auto_ria.dao.PremiumPlanDaoSQL;
import com.example.auto_ria.enums.EAccountType;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.models.SellerSQL;
import com.example.auto_ria.models.premium.PremiumPlan;
import com.example.auto_ria.models.requests.SetPaymentSourceRequest;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Price;
import com.stripe.model.Subscription;
import com.stripe.param.*;
import lombok.AllArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class StripeService {

    private Environment environment;

    private PremiumPlanDaoSQL premiumPlanDaoSQL;

    public void createPayment(SetPaymentSourceRequest body, SellerSQL sellerSQL) {
        try {
            Stripe.apiKey = environment.getProperty("Stripe.ApiKey");

            String stripeId = sellerSQL.getPaymentSource();
            boolean stripePresent = sellerSQL.isPaymentSourcePresent();
            Customer payer = Customer.retrieve(stripeId);
            String defaultSource = payer.getDefaultSource();


            String paymentToken = null;
            String customerId = null;

            if (body.isUseDefaultCard() && body.isSetAsDefaultCard()) {
                throw new CustomException("isUseDefaultCard: true and setAsDefaultCard: true " +
                        "The mentioned source would be already defined as default source", HttpStatus.BAD_REQUEST);
            }

            if (body.isUseDefaultCard() && defaultSource == null) {
                throw new CustomException("No default source is present. " +
                        "Attach a card to your account to make your payments faster", HttpStatus.BAD_REQUEST);
            }

            if (body.isSetAsDefaultCard() && defaultSource != null) {
                throw new CustomException("Default source is already defined. " +
                        "You can change it at any moment at - Http//3000/attach-card", HttpStatus.BAD_REQUEST);
            }

            try {
                if (stripePresent && defaultSource != null && body.isUseDefaultCard()) {
                    System.out.println("first");
                    paymentToken = payer.getDefaultSource();
                    customerId = stripeId;
                    System.out.println(paymentToken);
                    System.out.println(customerId);
                }

                if (stripePresent && !body.isUseDefaultCard() && !body.isSetAsDefaultCard()) { //todo
                    System.out.println("first1");
                    paymentToken = body.getToken();
                    customerId = stripeId;

                }

                if (stripePresent && defaultSource == null && body.isSetAsDefaultCard()) {
                    System.out.println("first2");
                    Customer stripeCustomer = Customer.retrieve(stripeId);

                    Map<String, Object> params = new HashMap<>();
                    params.put("source", body.getToken());

                    stripeCustomer.update(params);

                    paymentToken = body.getToken();
                    customerId = stripeId;

                }

                if (!stripePresent && !body.isSetAsDefaultCard()) {
                    System.out.println("first3");
                    Customer customer = Customer.create(
                            CustomerCreateParams.builder()
                                    .setName(sellerSQL.getName() + " " + sellerSQL.getLastName())
                                    .setEmail(sellerSQL.getEmail())
                                    .build()
                    );
                    paymentToken = body.getToken();
                    customerId = customer.getId();
                }

                if (!stripePresent && body.isSetAsDefaultCard()) {
                    System.out.println("first4");
                    Customer customer = Customer.create(
                            CustomerCreateParams.builder()
                                    .setName(sellerSQL.getName() + " " + sellerSQL.getLastName())
                                    .setEmail(sellerSQL.getEmail())
                                    .setSource(body.getToken())
                                    .build()
                    );
                    paymentToken = body.getToken();
                    customerId = customer.getId();
                }
            } catch (Exception e) {
                throw new CustomException("Please check your parameters", HttpStatus.CONFLICT);

            }

            if (paymentToken != null && customerId != null) {

                if (body.isAutoPay()) {
                    List<Object> items = new ArrayList<>();
                    Map<String, Object> item1 = new HashMap<>();
                    item1.put(
                            "price", "price_1OCqatAe4RILjJWGmXhBnHeX"
                    );
                    items.add(item1);

                    Map<String, Object> params1 = new HashMap<>();
                    params1.put("customer", customerId);
                    params1.put("items", items);
                    params1.put("collection_method", SubscriptionListParams.CollectionMethod.CHARGE_AUTOMATICALLY);

                    Subscription subscription = Subscription.create(params1);
                    System.out.println(subscription);
                    System.out.println("subscription----------------------------");
                    //todo cron delete long expired subscriptions

                    PremiumPlan premiumPlan = premiumPlanDaoSQL.findBySellerId(sellerSQL.getId());

                    if (premiumPlan != null) {
                        premiumPlan.setAutoPayments(body.isAutoPay());
                        premiumPlan.setStartDate(LocalDate.now());
                        premiumPlan.setEndDate(LocalDate.now().plusMonths(1));
                        premiumPlan.setSubId(subscription.getId());
                        premiumPlan.setActive(true);

                    } else {
                        premiumPlanDaoSQL.save(PremiumPlan.builder()
                                .autoPayments(body.isAutoPay())
                                .startDate(LocalDate.now())
                                .endDate(LocalDate.now().plusMonths(1))
                                .sellerId(sellerSQL.getId())
                                .customerId(customerId)
                                .subId(subscription.getId())
                                .isActive(true)
                                .build());
                    }

                    sellerSQL.setAccountType(EAccountType.PREMIUM);

                } else {
                    Price price = Price.retrieve("price_1OCqatAe4RILjJWGmXhBnHeX");

                    PaymentIntentCreateParams createParams = new PaymentIntentCreateParams.Builder()
                            .setAmount(price.getUnitAmount())
                            .setCurrency("usd")
                            .setDescription("Premium bought")
                            .setPaymentMethod(paymentToken)
                            .setCustomer(customerId)
                            .setConfirm(true)
                            .build();

                    PaymentIntent.create(createParams);

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

                    sellerSQL.setAccountType(EAccountType.PREMIUM);
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
