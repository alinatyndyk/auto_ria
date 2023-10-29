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
import lombok.AllArgsConstructor;
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

    @PostConstruct
    public void onApplicationStart() {
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
