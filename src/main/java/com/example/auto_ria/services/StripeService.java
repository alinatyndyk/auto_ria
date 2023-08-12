package com.example.auto_ria.services;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class StripeService {


    private String stripeApiKey = "sk_test_51MIX9gIAfGNWX8Hhl3mH4IFJladHRo1ErYUQv2ZEIWdfJIwKXvk5zHwOGUrntdnmJz7af89NUZFm94dVRYV00fRl00gqg3UAPA";

    public void chargeCreditCard(String token, BigDecimal amount) throws StripeException {
        Stripe.apiKey = stripeApiKey;

        Map<String, Object> params = new HashMap<>();
        params.put("amount", amount.multiply(BigDecimal.valueOf(100)).intValue()); // Convert amount to cents
        params.put("currency", "usd");
        params.put("source", token);

        Charge.create(params);
    }
}
