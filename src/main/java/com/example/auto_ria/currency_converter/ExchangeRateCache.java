package com.example.auto_ria.currency_converter;

public class ExchangeRateCache {
    private static Double usdBuy;
    private static Double usdSell;
    private static Double eurBuy;
    private static Double eurSell;

    public static synchronized void updateExchangeRates(Double usdBuy, Double usdSell, Double eurBuy, Double eurSell) {
        ExchangeRateCache.usdBuy = usdBuy;
        ExchangeRateCache.usdSell = usdSell;
        ExchangeRateCache.eurBuy = eurBuy;
        ExchangeRateCache.eurSell = eurSell;
    }

    public static synchronized Double getUsdBuy() {
        return usdBuy;
    }

    public static synchronized Double getUsdSell() {
        return usdSell;
    }

    public static synchronized Double getEurBuy() {
        return eurBuy;
    }

    public static synchronized Double getEurSell() {
        return eurSell;
    }
}
