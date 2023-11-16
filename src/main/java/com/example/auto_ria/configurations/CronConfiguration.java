package com.example.auto_ria.configurations;

import com.example.auto_ria.currency_converter.ExchangeRateCache;
import com.example.auto_ria.dao.AdministratorDaoSQL;
import com.example.auto_ria.dao.CustomerDaoSQL;
import com.example.auto_ria.dao.ManagerDaoSQL;
import com.example.auto_ria.dao.UserDaoSQL;
import com.example.auto_ria.dao.authDao.AdminAuthDaoSQL;
import com.example.auto_ria.dao.authDao.CustomerAuthDaoSQL;
import com.example.auto_ria.dao.authDao.ManagerAuthDaoSQL;
import com.example.auto_ria.dao.authDao.SellerAuthDaoSQL;
import com.example.auto_ria.enums.ECurrency;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.models.responses.CurrencyResponse;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.net.RequestOptions;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.SourceCreateParams;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Configuration
@EnableScheduling
@Async
@AllArgsConstructor
public class CronConfiguration {

    private UserDaoSQL sellerDaoSQL;
    private CustomerDaoSQL customerDaoSQL;
    private ManagerDaoSQL managerDaoSQL;
    private AdministratorDaoSQL administratorDaoSQL;

    private SellerAuthDaoSQL sellerAuthDaoSQL;
    private CustomerAuthDaoSQL customerAuthDaoSQL;
    private ManagerAuthDaoSQL managerAuthDaoSQL;
    private AdminAuthDaoSQL adminAuthDaoSQL;

    private Environment environment;


    @SneakyThrows
    @PostConstruct
    public void onApplicationStart() {

        Stripe.apiKey = environment.getProperty("Stripe.ApiKey");

        Customer stripeCustomer = Customer.retrieve("cus_P0TXHOkBLbkHcg");

//        Map<String, Object> paramCharge = new HashMap<>();
//        paramCharge.put("amount", 5000);
//        paramCharge.put("currency", "usd");
//        paramCharge.put("customer", stripeCustomer.getId());
        System.out.println(stripeCustomer.getInvoiceSettings().getDefaultPaymentMethod());
        System.out.println(stripeCustomer.getDefaultSource());

        PaymentIntentCreateParams createParams = new PaymentIntentCreateParams.Builder()
                .setAmount(Long.parseLong("90000"))
                .setCurrency("usd")
                .setDescription("From Customer")
                .setCustomer(stripeCustomer.getId())
                .setPaymentMethod(stripeCustomer.getInvoiceSettings().getDefaultPaymentMethod())
                .setConfirm(true)
                .build();

        PaymentIntent paymentIntent = PaymentIntent.create(createParams);
//
//        Map<String, Object> cardParams = new HashMap<String, Object>();
//        cardParams.put("number", "4242424242424242");
//        cardParams.put("exp_month", "12");
//        cardParams.put("exp_year", "2025");
//        cardParams.put("cvc", "123");
//
//        System.out.println(68);
//
//        Map<String, Object> tokenParams = new HashMap<String, Object>();
//
//        tokenParams.put("card", cardParams);
//
//        System.out.println(74);
//        Token token = Token.create(cardParams);
//        System.out.println(76);

//        Map<String, Object> tokenParams = new HashMap<String, Object>();
//        Map<String, Object> cardParams = new HashMap<String, Object>();
//        cardParams.put("number", "4242424242424242");
//        cardParams.put("exp_month", "12");
//        cardParams.put("exp_year", "2025");
//        cardParams.put("cvc", "123");
//
//        tokenParams.put("card", cardParams);
//
//        Token token = Token.create(tokenParams);
//
//        Map<String, Object> source = new HashMap<String, Object>();
//        source.put("source", token.getId());
//        System.out.println(80);

//        SourceCreateParams sourceCreateParams = SourceCreateParams.builder()
//                .setToken(token.getId())
//                .build();

//        Source source1 = Source.create(source);
//
//        Customer customer = Customer.create(
//                CustomerCreateParams.builder()
//                        .setName("name1")
//                        .setEmail("name1@gmail.com")
//                        .setSource(source1.getId())
//                        .build()
//        );


//        Map<String, Object> paymentMethodParams = new HashMap<>();
//        paymentMethodParams.put("type", "card");
//        paymentMethodParams.put("card", Collections.singletonMap("token", "tok_visa"));
//
//        PaymentMethod paymentMethod = PaymentMethod.create(paymentMethodParams);
//
//        PaymentIntentCreateParams createParams = new PaymentIntentCreateParams.Builder()
//                .setAmount(Long.parseLong("8000"))
//                .setCurrency("usd")
//                .setDescription("from front")
//                .setPaymentMethod(paymentMethod.getId())
//                .setConfirm(true)
//                .build();
//
//
//        PaymentIntent paymentIntent = PaymentIntent.create(createParams);

        // todo save customer id
        Stripe.apiKey = environment.getProperty("Stripe.ApiKey");

//        List<Object> events = new ArrayList<Object>();
//        events.add("invoice.payment_failed");
//
//        Map<String, Object> params = new HashMap<>();
//        params.put("enabled_events", events);
////        params.put("url", "https://webhook.site/e038300e-b72e-49f5-8b67-14c80d1ea5eb");
//        params.put("url", "http://localhost:8080/cars/webhooks/stripe");
//
//        WebhookEndpoint webhookEndpoint = WebhookEndpoint.create(params);
//
//        System.out.println(webhookEndpoint);
//        System.out.println("webhookEndpoint");

        getCurrencyRates();
        deleteUnactivatedAccounts();
        deleteExpiredTokens();
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void getCurrencyRates() {
        try {

            RestTemplate restTemplate = new RestTemplate();

            String api = environment.getProperty("privat.bank.api");

            CurrencyResponse[] exchangeRates;
            if (api != null) {
                exchangeRates = restTemplate.getForObject(api, CurrencyResponse[].class);
            } else {
                throw new NullPointerException("Privatbank api could not be accessed. URL is null");
            }

            Double UsdBuy = null;
            Double UsdSell = null;
            Double EURBuy = null;
            Double EURSell = null;


            assert exchangeRates != null;
            for (CurrencyResponse rate : exchangeRates) {

                if (rate.getCcy() == ECurrency.USD) {
                    UsdBuy = rate.getBuy();
                    UsdSell = rate.getSale();
                } else if (rate.getCcy() == ECurrency.EUR) {
                    EURBuy = rate.getBuy();
                    EURSell = rate.getSale();
                }

            }

            ExchangeRateCache.updateExchangeRates(UsdBuy, UsdSell, EURBuy, EURSell);
        } catch (Exception e) {
            throw new CustomException("Error while getting exchange rates", HttpStatus.EXPECTATION_FAILED);
        }
    }

    @Scheduled(cron = "0 0 0 */2 * *")
    public void deleteUnactivatedAccounts() {
        try {
            LocalDate twoDaysAgo = LocalDate.now().minusDays(2);
            LocalDateTime twoDaysAgoDateTime = LocalDateTime.of(twoDaysAgo, LocalTime.MIDNIGHT);

            sellerDaoSQL.deleteAllByIsActivatedFalseAndCreatedAtBefore(twoDaysAgoDateTime);
            customerDaoSQL.deleteAllByIsActivatedFalseAndCreatedAtBefore(twoDaysAgoDateTime);
            administratorDaoSQL.deleteAllByIsActivatedFalseAndCreatedAtBefore(twoDaysAgoDateTime);
            managerDaoSQL.deleteAllByIsActivatedFalseAndCreatedAtBefore(twoDaysAgoDateTime);
        } catch (Exception e) {
            throw new CustomException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @Scheduled(cron = "0 0 3 * * *")
    public void deleteExpiredTokens() {
        try {
            LocalDate now = LocalDate.now();
            LocalDateTime nowDateTime = LocalDateTime.of(now, LocalTime.now()).minusHours(24);

            sellerAuthDaoSQL.deleteAllByCreatedAtBefore(nowDateTime);
            customerAuthDaoSQL.deleteAllByCreatedAtBefore(nowDateTime);
            managerAuthDaoSQL.deleteAllByCreatedAtBefore(nowDateTime);
            adminAuthDaoSQL.deleteAllByCreatedAtBefore(nowDateTime);
        } catch (Exception e) {
            throw new CustomException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
}
