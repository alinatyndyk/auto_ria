package com.example.auto_ria.models.responses.car;

import java.time.LocalDate;
import java.util.List;

import com.example.auto_ria.enums.EBrand;
import com.example.auto_ria.enums.ECurrency;
import com.example.auto_ria.enums.EModel;
import com.example.auto_ria.models.responses.user.UserCarResponse;

import lombok.Builder;
import lombok.Data;

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
    private Boolean isActivated;
    private UserCarResponse user;

    private double priceUAH;
    private double priceEUR;
    private double priceUSD;

    private LocalDate createdAt;

    @Builder
    public CarResponse(int id, EBrand brand, EModel model, int powerH, String city,
            String region, String price, ECurrency currency,
            List<String> photo, String description, UserCarResponse user, Boolean isActivated,
            double priceUAH, double priceEUR, double priceUSD, LocalDate createdAt) {
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
        this.isActivated = isActivated;
        this.user = user;
        if (user.getName().equals("Auto.Ria Services")) {
            user.setNumber("+380 63 748 73 02");
        }
        this.priceUAH = priceUAH;
        this.priceEUR = priceEUR;
        this.priceUSD = priceUSD;
        this.createdAt = createdAt;
    }
}
