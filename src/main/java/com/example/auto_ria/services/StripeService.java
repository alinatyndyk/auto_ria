package com.example.auto_ria.services;

import com.example.auto_ria.models.SellerSQL;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class StripeService {

    public void createPayment(SellerSQL sellerSQL) {
        try {
            Stripe.apiKey = "sk_test_51Nf481Ae4RILjJWGnrK0ByYfSZcW7FRzMZorxUraOwKjvOJEcejN4W05quGPkXf3AhJ1mtkaYgs5JvmEAonLCcpE00zeZqzgEI";

            Map<String, Object> paymentMethodParams = new HashMap<>();
            paymentMethodParams.put("type", "card");
            paymentMethodParams.put("card", Collections.singletonMap("token", "tok_visa"));

            PaymentMethod paymentMethod = PaymentMethod.create(paymentMethodParams);

            Map<String, Object> extraParams = new HashMap<>();
            List<String> paymentMethodTypes = new ArrayList<>();
            paymentMethodTypes.add("card");
            extraParams.put("payment_method_types", paymentMethodTypes);

            PaymentIntentCreateParams createParams = new PaymentIntentCreateParams.Builder()
                    .setCurrency("usd")
                    .setCustomer(sellerSQL.getEmail())
                    .setDescription("AutoRia premium plan")
                    .setAmount(Long.parseLong("50000"))
                    .putAllExtraParam(extraParams)
                    .setPaymentMethod(paymentMethod.getId())
                    .setConfirm(true)
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(createParams);

            if (paymentIntent.getStatus().equals("succeeded")) {
                ResponseEntity.ok("Payment completed successfully");
            } else {
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Payment failed");
            }
        } catch (StripeException e) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
//-----------------------------------------------------------------------------------------------
//    @PostMapping("/create-payment-intent")
//    public ResponseEntity<?> createPaymentIntent(@RequestBody Map<String, Object> request) {
//        try {
//
//            Stripe.apiKey = "sk_test_51Nf481Ae4RILjJWGnrK0ByYfSZcW7FRzMZorxUraOwKjvOJEcejN4W05quGPkXf3AhJ1mtkaYgs5JvmEAonLCcpE00zeZqzgEI";
//
//            Integer amount = (Integer) request.get("amount");
//            Long amountLong = amount.longValue();
//
//            Map<String, Object> extraParams = new HashMap<>();
//            List<String> paymentMethodTypes = new ArrayList<>();
//            paymentMethodTypes.add("card");
//            extraParams.put("payment_method_types", paymentMethodTypes);
//
//            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
//                    .setCurrency("usd")
//                    .setAmount(amountLong)
//                    .putAllExtraParam(extraParams)
//                    .build();
//
//            PaymentIntent paymentIntent = PaymentIntent.create(params);
//
//            List<String> paymentMethods = paymentIntent.getPaymentMethodTypes();
//            String paymentIntentId = paymentIntent.getId();
//
//            return ResponseEntity.ok().body(Map.of("paymentIntentId", paymentIntentId, "paymentMethods", paymentMethods));
//        } catch (StripeException e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
//        }
//    }
//
//    @PostMapping("/create-payment-method")
//    public ResponseEntity<?> createPaymentMethod() {
//        try {
//            Stripe.apiKey = "sk_test_51Nf481Ae4RILjJWGnrK0ByYfSZcW7FRzMZorxUraOwKjvOJEcejN4W05quGPkXf3AhJ1mtkaYgs5JvmEAonLCcpE00zeZqzgEI";
//
//            Map<String, Object> paymentMethodParams = new HashMap<>();
//            paymentMethodParams.put("type", "card");
//            paymentMethodParams.put("card", Collections.singletonMap("token", "tok_visa"));
//
//            PaymentMethod paymentMethod = PaymentMethod.create(paymentMethodParams);
//
//            PaymentIntentCreateParams createParams = new PaymentIntentCreateParams.Builder()
//                    .setAmount(Long.parseLong("1000000"))
//                    .setCurrency("usd")
//                    .setPaymentMethod(paymentMethod.getId())
//                    .setConfirm(true)
//                    .build();
//
//            PaymentIntent paymentIntent = PaymentIntent.create(createParams);
//
//            if (paymentIntent.getStatus().equals("succeeded")) {
//                return ResponseEntity.ok().body(Map.of("message", "Payment completed successfully"));
//            } else {
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Payment failed"));
//            }
//        } catch (StripeException e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
//        }
//    }
}
