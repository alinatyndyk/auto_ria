package com.example.auto_ria.services;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
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
    @Scheduled(fixedDelay = 20000)
    public void createPayment() {
        try {
            Stripe.apiKey = environment.getProperty("Stripe.ApiKey");

            Map<String, Object> paymentMethodParams = new HashMap<>();
            paymentMethodParams.put("type", "card");
            paymentMethodParams.put("card", Collections.singletonMap("token", "tok_visa"));
//            paymentMethodParams.put("card", Collections.singletonMap(
//                    "number", "4242424242424242" // Replace with actual card number
//            ));
//            paymentMethodParams.put("card", Collections.singletonMap(
//                    "exp_month", 10 // Replace with actual expiration month
//            ));
//            paymentMethodParams.put("card", Collections.singletonMap(
//                    "exp_year", 2027 // Replace with actual expiration year
//            ));
//            paymentMethodParams.put("card", Collections.singletonMap(
//                    "cvc", "123" // Replace with actual CVC
//            ));

            PaymentMethod paymentMethod = PaymentMethod.create(paymentMethodParams);

            PaymentIntentCreateParams createParams = new PaymentIntentCreateParams.Builder()
                    .setAmount(Long.parseLong("6000"))
                    .setCurrency("usd")
                    .setPaymentMethod(paymentMethod.getId())
                    .setConfirm(true)
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(createParams);

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
}
