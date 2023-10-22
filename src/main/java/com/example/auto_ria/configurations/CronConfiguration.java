package com.example.auto_ria.configurations;

import com.example.auto_ria.currency_converter.ExchangeRateCache;
import com.example.auto_ria.dao.AdministratorDaoSQL;
import com.example.auto_ria.dao.CustomerDaoSQL;
import com.example.auto_ria.dao.ManagerDaoSQL;
import com.example.auto_ria.dao.UserDaoSQL;
import com.example.auto_ria.enums.ECurrency;
import com.example.auto_ria.models.responses.CurrencyResponse;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.time.LocalDate;

@Configuration
@EnableScheduling
@Async
@AllArgsConstructor
public class CronConfiguration {

    private UserDaoSQL sellerDaoSQL;
    private CustomerDaoSQL customerDaoSQL;
    private ManagerDaoSQL managerDaoSQL;
    private AdministratorDaoSQL administratorDaoSQL;


    @PostConstruct
    public void onApplicationStart() {
        getCurrencyRates();
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void getCurrencyRates() {

        RestTemplate restTemplate = new RestTemplate();

        String api = "https://api.privatbank.ua/p24api/pubinfo?json&exchange&coursid=5"; //todo to env

        CurrencyResponse[] exchangeRates = restTemplate.getForObject(api, CurrencyResponse[].class);

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
    }

//    @Scheduled(cron = "0 0 */2 * *")
//    public void deleteUnactivatedAccounts() {
//// todo
//        LocalDate twoDaysAgo = LocalDate.now().minusDays(2);
//        sellerDaoSQL.findByCreatedAtBeforeAndIsActivatedFalse(twoDaysAgo);
//        customerDaoSQL.findByCreatedAtBeforeAndIsActivatedFalse(twoDaysAgo);
//        administratorDaoSQL.findByCreatedAtBeforeAndIsActivatedFalse(twoDaysAgo);
//        managerDaoSQL.findByCreatedAtBeforeAndIsActivatedFalse(twoDaysAgo);
//
//    }
}
