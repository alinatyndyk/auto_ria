package com.example.auto_ria.services;

import com.example.auto_ria.models.SellerSQL;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.AllArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@AllArgsConstructor
public class StripeService {

    private Environment environment;

    //    @Scheduled(cron = "0 0 0 1 *")
//    @Scheduled(fixedDelay = 20000)
    public void createPayment(String tokenId) {
        try {
            Stripe.apiKey = environment.getProperty("Stripe.ApiKey");

            Map<String, Object> paymentMethodParams = new HashMap<>();
            paymentMethodParams.put("type", "card");
            paymentMethodParams.put("card", Collections.singletonMap("token", tokenId));

            PaymentMethod paymentMethod = PaymentMethod.create(paymentMethodParams);

            PaymentIntentCreateParams createParams = new PaymentIntentCreateParams.Builder()
                    .setAmount(Long.parseLong("80000"))
                    .setCurrency("usd")
                    .setDescription("from front hello")
                    .setPaymentMethod(paymentMethod.getId())
                    .setConfirm(true)
                    .build();

            System.out.println("1111111111111111");
            PaymentIntent paymentIntent = PaymentIntent.create(createParams);
            System.out.println("2222222222222222222");

            if (!paymentIntent.getStatus().equals("succeeded")) {
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Payment failed"));
                return;
            }
        } catch (StripeException e) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
            return;
        }
        ResponseEntity.ok(Map.of("message", "Payment completed successfully"));
    }

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
