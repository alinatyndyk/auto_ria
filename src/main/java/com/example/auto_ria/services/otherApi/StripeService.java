package com.example.auto_ria.services.otherApi;

import com.example.auto_ria.dao.premium.PremiumPlanDaoSQL;
import com.example.auto_ria.dao.user.UserDaoSQL;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.models.premium.PremiumPlan;
import com.example.auto_ria.models.requests.SetPaymentSourceRequest;
import com.example.auto_ria.models.user.SellerSQL;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.SubscriptionCreateParams;
import com.stripe.param.SubscriptionUpdateParams;
import lombok.AllArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@AllArgsConstructor
public class StripeService {

    private Environment environment;
    private UserDaoSQL userDaoSQL;

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
                defaultSource = payer.getDefaultSource();
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

            if (body.isAutoPay() && !body.isSetAsDefaultCard() && !stripePresent) {
                throw new CustomException("Subscription requires a default card", HttpStatus.BAD_REQUEST);
            }

            try {
                if (stripePresent && defaultSource != null && body.isUseDefaultCard()) {
                    System.out.println("first");
                    paymentToken = defaultSource;
                    customerId = stripeId;
                } else if (stripePresent && !body.isSetAsDefaultCard()) {
                    System.out.println("first1");
                    paymentToken = body.getToken();
                    customerId = stripeId;

                } else if (stripePresent) {
                    System.out.println("first2");
                    assert stripeId != null;
                    Customer stripeCustomer = Customer.retrieve(stripeId);

                    Map<String, Object> params = new HashMap<>();
                    params.put("source", body.getToken());

                    stripeCustomer.update(params);

                    paymentToken = body.getToken();
                    customerId = stripeId;

                } else if (!body.isSetAsDefaultCard()) {
                    System.out.println("first3");
                    Customer customer = Customer.create(
                            CustomerCreateParams.builder()
                                    .setName(sellerSQL.getName() + sellerSQL.getLastName())
                                    .setEmail(sellerSQL.getEmail())
                                    .build()
                    );
                    paymentToken = body.getToken();
                    customerId = customer.getId();

                    sellerSQL.setPaymentSourcePresent(true);
                    sellerSQL.setPaymentSource(customerId);

                    userDaoSQL.save(sellerSQL);
                } else if (body.isSetAsDefaultCard()) {
                    System.out.println("first4");
                    Customer customer = Customer.create(
                            CustomerCreateParams.builder()
                                    .setName(sellerSQL.getName() + sellerSQL.getLastName())
                                    .setEmail(sellerSQL.getEmail())
                                    .setSource(body.getToken())
                                    .build()
                    );
                    paymentToken = body.getToken();
                    customerId = customer.getId();

                    sellerSQL.setPaymentSourcePresent(true);
                    sellerSQL.setPaymentSource(customerId);

                    userDaoSQL.save(sellerSQL);
                } else {
                    throw new CustomException("Payment params invalid", HttpStatus.BAD_REQUEST);
                }
            } catch (Exception e) {
                throw new CustomException("Please check your parameters" + e.getMessage(), HttpStatus.CONFLICT);

            }

            if (paymentToken != null && customerId != null) {
                if (body.isAutoPay()) {

                    Price price = Price.retrieve("price_1OCqatAe4RILjJWGmXhBnHeX");

                    Subscription subscription = Subscription.create(SubscriptionCreateParams.builder()
                            .setCustomer(customerId)
                            .addItem(SubscriptionCreateParams.Item.builder().setPrice(price.getId()).build())
                            .build());

                    PremiumPlan premiumPlan = premiumPlanDaoSQL.findBySellerId(sellerSQL.getId());

                    if (premiumPlan != null) {
                        premiumPlan.setAutoPayments(body.isAutoPay());
                        premiumPlan.setStartDate(LocalDate.now());
                        premiumPlan.setEndDate(LocalDate.now().plusMonths(1));
                        premiumPlan.setSubId(subscription.getId());
                        premiumPlan.setActive(true);
                        premiumPlanDaoSQL.save(premiumPlan);

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

                } else {
                    Price price = Price.retrieve("price_1OCqatAe4RILjJWGmXhBnHeX");
                    Map<String, Object> paymentMethodParams = new HashMap<>();
                    paymentMethodParams.put("type", "card");

                    PaymentIntentCreateParams createParams;

                    if (body.isUseDefaultCard() || defaultSource != null) {
                        String defaultPaymentMethod = Customer.retrieve(customerId).getDefaultSource();
                        paymentMethodParams.put("card", defaultPaymentMethod);

                        createParams = new PaymentIntentCreateParams.Builder()
                                .setAmount(price.getUnitAmount())
                                .setCurrency("usd")
                                .setDescription("Premium 1m bought")
                                .setCustomer(customerId)
                                .setConfirm(true)
                                .build();

                    } else {
                        paymentMethodParams.put("card", Collections.singletonMap("token", paymentToken));

                        PaymentMethod paymentMethod = PaymentMethod.create(paymentMethodParams);

                        createParams = new PaymentIntentCreateParams.Builder()
                                .setAmount(price.getUnitAmount())
                                .setCurrency("chf")
                                .setDescription("Premium 1m bought")
                                .setPaymentMethod(paymentMethod.getId())
                                .setCustomer(customerId)
                                .setConfirm(true)
                                .build();
                    }

                    PaymentIntent.create(createParams);

                    PremiumPlan premiumPlan = premiumPlanDaoSQL.findBySellerId(sellerSQL.getId());
                    if (premiumPlan != null) {
                        premiumPlan.setAutoPayments(body.isAutoPay());
                        premiumPlan.setStartDate(LocalDate.now());
                        premiumPlan.setEndDate(LocalDate.now().plusMonths(1));
                        premiumPlan.setSubId(null);
                        premiumPlan.setActive(true);
                        premiumPlanDaoSQL.save(premiumPlan);
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
                }

                System.out.println(224);
            } else {
                System.out.println(225);
                throw new CustomException("Source attachment fail: credential provided is null or invalid", HttpStatus.BAD_REQUEST);
            }
        } catch (CustomException e) {
            System.out.println(228);
            throw new CustomException(e.getMessage(), e.getStatus());
        } catch (StripeException e) {
            System.out.println(231);
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
            return;
        } catch (Exception e) {
            System.out.println(235);
            throw new CustomException("Failed to create payment", HttpStatus.EXPECTATION_FAILED);
        }
        System.out.println(238);
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
