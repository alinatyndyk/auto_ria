package com.example.auto_ria.models.responses.car;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MiddlePriceResponse {

    private double middleInUAH;
    private double middleInEUR;
    private double middleInUSD;

}
