package com.example.auto_ria.configurations;

import com.example.auto_ria.currency_converter.ExchangeRateCache;
import com.example.auto_ria.dao.auth.AdminAuthDaoSQL;
import com.example.auto_ria.dao.auth.CustomerAuthDaoSQL;
import com.example.auto_ria.dao.auth.ManagerAuthDaoSQL;
import com.example.auto_ria.dao.auth.SellerAuthDaoSQL;
import com.example.auto_ria.dao.premium.PremiumPlanDaoSQL;
import com.example.auto_ria.dao.user.AdministratorDaoSQL;
import com.example.auto_ria.dao.user.CustomerDaoSQL;
import com.example.auto_ria.dao.user.ManagerDaoSQL;
import com.example.auto_ria.dao.user.UserDaoSQL;
import com.example.auto_ria.enums.EAccountType;
import com.example.auto_ria.enums.ECurrency;
import com.example.auto_ria.enums.EMail;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.mail.FMService;
import com.example.auto_ria.models.user.SellerSQL;
import com.example.auto_ria.models.premium.PremiumPlan;
import com.example.auto_ria.models.responses.CurrencyResponse;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    private PremiumPlanDaoSQL premiumPlanDaoSQL;

    private FMService mailer;

    private Environment environment;


    @SneakyThrows
    @PostConstruct
    public void onApplicationStart() {
        // webhook
//        List<Object> events = new ArrayList<Object>();
//        events.add("invoice.payment_failed");
//
//        Map<String, Object> params = new HashMap<>();
//        params.put("enabled_events", events);
//        params.put("url", "https://webhook.site/e038300e-b72e-49f5-8b67-14c80d1ea5eb");
//
//        WebhookEndpoint webhookEndpoint = WebhookEndpoint.create(params);

        invoiceExpiredPremiumAccounts();
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

    @Scheduled(cron = "0 0 0 * * *")
    public void invoiceExpiredPremiumAccounts() {
        try {
            List<PremiumPlan> premiumPlans = premiumPlanDaoSQL.findAll();
            premiumPlans.forEach(premiumPlan -> {
                if (premiumPlan.getEndDate().isAfter(LocalDate.now())) {
                    System.out.println(premiumPlan.getEndDate().isAfter(LocalDate.now()));

                    premiumPlan.setActive(false);
                    premiumPlanDaoSQL.save(premiumPlan);
                    Optional<SellerSQL> owner = sellerDaoSQL.findById(premiumPlan.getSellerId());

                    if (owner.isPresent()) {
                        owner.get().setAccountType(EAccountType.BASIC);
                        sellerDaoSQL.save(owner.get());

                        Map<String, Object> args = new HashMap<>();
                        args.put("name", owner.get().getName() + owner.get().getLastName());
                        args.put("url", "http://localhost:3000/");

                        mailer.sendEmail(owner.get().getEmail(), EMail.PREMIUM_END, args);
                    }
                }
            });

        } catch (Exception e) {
            System.out.println(e.getMessage());
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

    @Scheduled(cron = "0 0 0 */3 * *")
    public void deleteLongNonPayedSubscriptions() {
        try {
            LocalDate minusThreeMonths = LocalDate.now().minusMonths(3);
            premiumPlanDaoSQL.deleteByEndDateBefore(minusThreeMonths);
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
