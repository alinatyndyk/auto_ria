package com.example.auto_ria.models.responses;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExchangeRateResponse {
    private Double usdBuy;
    private Double usdSell;
    private Double eurBuy;
    private Double eurSell;
}
