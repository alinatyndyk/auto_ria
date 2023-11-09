package com.example.auto_ria.models.responses;

import com.example.auto_ria.enums.EBrand;
import com.example.auto_ria.enums.ECurrency;
import com.example.auto_ria.enums.EModel;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class CarResponse {

    private int id;
    private EBrand brand;
    private EModel model;
    private int powerH;
    private String city;
    private String region;
    private String price;
    private ECurrency currency;
    private List<String> photo;
    private String description;
    private SellerResponse seller;

    private double priceUAH;
    private double priceEUR;
    private double priceUSD;

    @Builder
    public CarResponse(int id, EBrand brand, EModel model, int powerH, String city,
                       String region, String price, ECurrency currency,
                       List<String> photo, String description, SellerResponse seller,
                       double priceUAH, double priceEUR, double priceUSD) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.powerH = powerH;
        this.city = city;
        this.region = region;
        this.price = price;
        this.currency = currency;
        this.photo = photo;
        this.description = description;
        this.seller = seller;
        if (seller.getName().equals("Auto.Ria Services")) {
            seller.setNumber("+380 63 748 73 02");
        }
        this.priceUAH = priceUAH;
        this.priceEUR = priceEUR;
        this.priceUSD = priceUSD;
    }
}
