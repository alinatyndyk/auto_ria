package com.example.auto_ria.services;

import com.example.auto_ria.models.SellerSQL;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.param.PaymentIntentCreateParams;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class StripeService {

    public ResponseEntity<?> createPayment(SellerSQL sellerSQL) {
        try {
            Stripe.apiKey = "sk_test_51Nf481Ae4RILjJWGnrK0ByYfSZcW7FRzMZorxUraOwKjvOJEcejN4W05quGPkXf3AhJ1mtkaYgs5JvmEAonLCcpE00zeZqzgEI";

            Map<String, Object> paymentMethodParams = new HashMap<>();
            paymentMethodParams.put("type", "card");
            paymentMethodParams.put("card", Collections.singletonMap("token", "tok_visa"));

            PaymentMethod paymentMethod = PaymentMethod.create(paymentMethodParams);

            PaymentIntentCreateParams createParams = new PaymentIntentCreateParams.Builder()
                    .setAmount(Long.parseLong("5000"))
                    .setCurrency("usd")
                    .setPaymentMethod(paymentMethod.getId())
                    .setConfirm(true)
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(createParams);

            if (!paymentIntent.getStatus().equals("succeeded")) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Payment failed"));
            }
        } catch (StripeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
        return ResponseEntity.ok(Map.of("message", "Payment completed successfully"));
    }
}
