package com.example.auto_ria.models.responses;

import com.example.auto_ria.enums.ECurrency;
import lombok.Data;

@Data
public class CurrencyResponse {
    private ECurrency ccy;
    private ECurrency base_ccy;
    private double buy;
    private double sale;

}
