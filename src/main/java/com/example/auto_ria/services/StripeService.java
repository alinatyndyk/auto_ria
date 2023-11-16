package com.example.auto_ria.services;

import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.models.SellerSQL;
import com.example.auto_ria.models.requests.SetPaymentSourceRequest;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.model.Subscription;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.AllArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@AllArgsConstructor
public class StripeService {

    private Environment environment;

    public void createPayment(SetPaymentSourceRequest body, SellerSQL sellerSQL) {
        try {
            Stripe.apiKey = environment.getProperty("Stripe.ApiKey");

            String stripeId = sellerSQL.getPaymentSource();
            boolean stripePresent = sellerSQL.isPaymentSourcePresent();
            Customer payer = Customer.retrieve(stripeId);
            String defaultSource = payer.getDefaultSource(); // is it null


            List<Object> items = new ArrayList<>();
            Map<String, Object> item1 = new HashMap<>();
            item1.put(
                    "price",
                    "price_1OCqatAe4RILjJWGmXhBnHeX"
            );
            items.add(item1);
            Map<String, Object> params = new HashMap<>();
            params.put("customer", stripeId);
            params.put("items", items);
//            params.put("trial_end", (System.currentTimeMillis() / 1000) + (3 * 24 * 60 * 60));
            params.put("trial_end", (System.currentTimeMillis() / 1000) + (3 * 60));


            Subscription subscription =
                    Subscription.create(params);

            System.out.println(subscription);
            System.out.println("subscription----------------------------");


//            String paymentToken = null;
//            String customerId = null;
//
//            if (body.isUseDefaultCard() && body.isSetAsDefaultCard()) {
//                throw new CustomException("isUseDefaultCard: true and setAsDefaultCard: true " +
//                        "The mentioned source would be already defined as default source", HttpStatus.BAD_REQUEST);
//            }
//
//            if (body.isUseDefaultCard() && defaultSource == null) {
//                throw new CustomException("No default source is present. " +
//                        "Attach a card to your account to make your payments faster", HttpStatus.BAD_REQUEST);
//            }
//
//            if (body.isSetAsDefaultCard() && defaultSource != null) {
//                throw new CustomException("Default source is already defined. " +
//                        "You can change it at any moment at - Http//3000/attach-card", HttpStatus.BAD_REQUEST);
//            }
//
//            try {
//                if (stripePresent && defaultSource != null && body.isUseDefaultCard()) {
//                    System.out.println("first");
//                    paymentToken = payer.getInvoiceSettings().getDefaultPaymentMethod();
//                    customerId = stripeId;
//                }
//
//                if (stripePresent && !body.isUseDefaultCard() && !body.isSetAsDefaultCard()) { //todo
//                    System.out.println("first1");
//                    paymentToken = body.getToken();
//                    customerId = stripeId;
//
//                }
//
//                if (stripePresent && defaultSource == null && body.isSetAsDefaultCard()) {
//                    System.out.println("first2");
//                    Customer stripeCustomer = Customer.retrieve(stripeId);
//
//                    Map<String, Object> params = new HashMap<>();
//                    params.put("source", body.getToken());
//
//                    stripeCustomer.update(params);
//
//                    paymentToken = body.getToken();
//                    customerId = stripeId;
//
//                }
//
//
//                if (!stripePresent && !body.isSetAsDefaultCard()) {
//                    System.out.println("first3");
//                    Customer customer = Customer.create(
//                            CustomerCreateParams.builder()
//                                    .setName(sellerSQL.getName() + " " + sellerSQL.getLastName())
//                                    .setEmail(sellerSQL.getEmail())
//                                    .build()
//                    );
//                    paymentToken = body.getToken();
//                    customerId = customer.getId();
//                }
//
//                if (!stripePresent && body.isSetAsDefaultCard()) {
//                    System.out.println("first4");
//                    Customer customer = Customer.create(
//                            CustomerCreateParams.builder()
//                                    .setName(sellerSQL.getName() + " " + sellerSQL.getLastName())
//                                    .setEmail(sellerSQL.getEmail())
//                                    .setSource(body.getToken())
//                                    .build()
//                    );
//                    paymentToken = body.getToken();
//                    customerId = customer.getId();
//                }
//            } catch (Exception e) {
//                throw new CustomException("Please check your parameters", HttpStatus.CONFLICT);
//
//            }
//
//            if (paymentToken != null && customerId != null) {
//                PaymentIntentCreateParams createParams = new PaymentIntentCreateParams.Builder()
//                        .setAmount(Long.parseLong("8000"))
//                        .setCurrency("usd")
//                        .setDescription("Premium bought")
//                        .setPaymentMethod(paymentToken)
//                        .setCustomer(customerId)
//                        .setConfirm(true)
//                        .build();
//
//                PaymentIntent.create(createParams);
//            } else {
//                throw new CustomException("Source attachment fail: credential provided is null or invalid", HttpStatus.BAD_REQUEST);
//            }
        } catch (StripeException e) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
            return;
        }
        ResponseEntity.ok(Map.of("message", "Payment completed successfully"));
    }

    //todo add customer to stripe and create card method.
    // + add cardId field to Seller + isPresentCard field
    //todo buy premium, 1 - if cardID present then retrieve card and take money,
    //                  2 - if cardID NOT present then Stripe checkout, + sub or one

    @Scheduled(fixedDelay = 20000)
//    public void createSubscription(SellerSQL seller) {
    public void createSubscription() {
        try {
            Stripe.apiKey = environment.getProperty("Stripe.ApiKey");

//            Map<String, Object> customerParams = new HashMap<String, Object>();
//            customerParams.put("email", seller.getEmail());
//            customerParams.put("email", "seller.getEmail()");

            Customer customer = Customer.create(CustomerCreateParams.builder()
                    .setEmail("seller.getEmail()")
                    .setName("seller name")
                    .setPhone("seller phone")
                    .build());


        } catch (Exception e) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
            return;
        }
        ResponseEntity.ok(Map.of("message", "Payment completed successfully"));
    }
}
