package com.example.auto_ria.services.currency;

import com.example.auto_ria.currency_converter.ExchangeRateCache;
import com.example.auto_ria.enums.ECurrency;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.models.responses.currency.CurrencyConverterResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class CurrencyConverterService {

    public CurrencyConverterResponse convert(ECurrency currency, String price) {

        try {
            HashMap<ECurrency, Double> converterRes = new HashMap<>();

            if (currency.equals(ECurrency.UAH)) {
                converterRes.put(ECurrency.UAH, Double.valueOf(price));
                converterRes.put(ECurrency.USD, Double.parseDouble(price) / ExchangeRateCache.getUsdBuy());
                converterRes.put(ECurrency.EUR, Double.parseDouble(price) / ExchangeRateCache.getEurBuy());
            } else if (currency.equals(ECurrency.USD)) {
                converterRes.put(ECurrency.USD, Double.valueOf(price));
                converterRes.put(ECurrency.UAH, Double.parseDouble(price) * ExchangeRateCache.getUsdSell());
                converterRes.put(ECurrency.EUR, Double.parseDouble(price) * ExchangeRateCache.getUsdSell() / ExchangeRateCache.getEurBuy());
            } else if (currency.equals(ECurrency.EUR)) {
                converterRes.put(ECurrency.EUR, Double.valueOf(price));
                converterRes.put(ECurrency.UAH, Double.parseDouble(price) * ExchangeRateCache.getEurBuy());
                converterRes.put(ECurrency.USD, Double.parseDouble(price) * ExchangeRateCache.getEurSell() / ExchangeRateCache.getUsdBuy());
            }

            return CurrencyConverterResponse.builder()
                    .currencyHashMap(converterRes).build();
        } catch (
                Exception e) {
            throw new CustomException("Failed to convert currency: " + e.getMessage(), HttpStatus.CONFLICT);
        }
    }

}
