package com.example.auto_ria.models.responses.currency;

import com.example.auto_ria.enums.ECurrency;
import lombok.Builder;
import lombok.Data;

import java.util.HashMap;

@Data
@Builder
public class CurrencyConverterResponse {
    private HashMap<ECurrency, Double> currencyHashMap;
}
