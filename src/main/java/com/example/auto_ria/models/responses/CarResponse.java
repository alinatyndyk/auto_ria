package com.example.auto_ria.models.responses;

import com.example.auto_ria.enums.ECurrency;
import com.example.auto_ria.enums.ERegion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class CarResponse {

    private String brand;
    private int powerH;
    private String city;
    private ERegion region;
    private String producer;
    private String price;
    private ECurrency currency;
    private List<String> photo;
    private String description;
    private double priceUAH;
    private double priceEUR;
    private double priceUSD;
}
