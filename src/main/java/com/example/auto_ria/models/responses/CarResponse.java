package com.example.auto_ria.models.responses;

import com.example.auto_ria.enums.EBrand;
import com.example.auto_ria.enums.ECurrency;
import com.example.auto_ria.enums.EModel;
import com.example.auto_ria.enums.ERegion;
import com.example.auto_ria.models.SellerSQL;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class CarResponse {

    private EBrand brand;
    private EModel model;
    private int powerH;
    private String city;
    private ERegion region;
    private String price;
    private ECurrency currency;
    private List<String> photo;
    private String description;
    private SellerResponse seller;

    private double priceUAH;
    private double priceEUR;
    private double priceUSD;

    private double middlePriceUAH;
    private double middlePriceEUR;
    private double middlePriceUSD;
}
