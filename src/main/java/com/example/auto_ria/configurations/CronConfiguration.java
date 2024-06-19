package com.example.auto_ria.configurations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import com.example.auto_ria.currency_converter.ExchangeRateCache;
import com.example.auto_ria.dao.auth.UserAuthDaoSQL;
import com.example.auto_ria.dao.premium.PremiumPlanDaoSQL;
import com.example.auto_ria.dao.user.UserDaoSQL;
import com.example.auto_ria.enums.EAccountType;
import com.example.auto_ria.enums.ECurrency;
import com.example.auto_ria.enums.EMail;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.mail.FMService;
import com.example.auto_ria.models.premium.PremiumPlan;
import com.example.auto_ria.models.responses.currency.CurrencyResponse;
import com.example.auto_ria.models.user.UserSQL;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

@Configuration
@EnableScheduling
@Async
@AllArgsConstructor
public class CronConfiguration {

    private UserDaoSQL userDaoSQL;
    private UserAuthDaoSQL userAuthDaoSQL;
    private PremiumPlanDaoSQL premiumPlanDaoSQL;

    private FMService mailer;
    private Environment environment;

    @SneakyThrows
    @PostConstruct
    public void onApplicationStart() {
        invoiceExpiredPremiumAccounts();
        getCurrencyRates();
        deleteUnactivatedAccounts();
        deleteExpiredTokens();
    }

    @SuppressWarnings("null")
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

                    premiumPlan.setActive(false);
                    premiumPlanDaoSQL.save(premiumPlan);
                    Optional<UserSQL> owner = userDaoSQL.findById(premiumPlan.getUserId());

                    if (owner.isPresent()) {
                        owner.get().setAccountType(EAccountType.BASIC);
                        userDaoSQL.save(owner.get());

                        Map<String, Object> args = new HashMap<>();
                        args.put("name", owner.get().getName() + owner.get().getLastName());
                        args.put("url", "http://localhost:3000/");

                        mailer.sendEmail(owner.get().getEmail(), EMail.PREMIUM_END, args);
                    }
                }
            });

        } catch (Exception e) {
            throw new CustomException("Error while getting exchange rates", HttpStatus.EXPECTATION_FAILED);
        }
    }

    @Scheduled(cron = "0 0 0 */2 * *")
    public void deleteUnactivatedAccounts() {
        try {
            LocalDate twoDaysAgo = LocalDate.now().minusDays(2);
            LocalDateTime twoDaysAgoDateTime = LocalDateTime.of(twoDaysAgo, LocalTime.MIDNIGHT);

            userDaoSQL.deleteAllByIsActivatedFalseAndCreatedAtBefore(twoDaysAgoDateTime);
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

            System.out.println(nowDateTime + "*******************");
            
            // userAuthDaoSQL.deleteAllByCreatedAtBefore(nowDateTime);
        } catch (Exception e) {
            throw new CustomException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
