package com.example.auto_ria.models;

import com.example.auto_ria.enums.EBrand;
import com.example.auto_ria.enums.ECurrency;
import com.example.auto_ria.enums.EModel;
import com.example.auto_ria.models.user.SellerSQL;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.EnumUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarSQL {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Enumerated(EnumType.STRING)
    private EBrand brand;

    private int powerH;

    private String city;

    private String region;

    private EModel model;

    @ElementCollection
    private List<String> photo = new ArrayList<>();

    @JsonManagedReference
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinTable(
            name = "seller_cars",
            joinColumns = @JoinColumn(name = "car_id"),
            inverseJoinColumns = @JoinColumn(name = "seller_id")
    )
    private SellerSQL seller;

    private String price;

    private ECurrency currency;

    private String description;

    private boolean isActivated;


    @Builder
    @SuppressWarnings("unused")
    public CarSQL(EBrand brand, Integer powerH, String city, String region,
                  EModel model, List<String> photo, SellerSQL seller, String price,
                  ECurrency currency, String description, boolean isActivated) {
        this.brand = brand;
        this.model = model;
        validateEnums();
        validateBrandAndModel();
        this.powerH = powerH;
        this.city = city;
        this.region = region;
        this.photo = photo;
        this.seller = seller;
        this.price = price;
        this.currency = currency;
        this.description = description;
        this.isActivated = isActivated;
    }

    private void validateBrandAndModel() {
        if (model.getBrand() != brand) {
            EModel[] fordModels = Arrays.stream(EModel.values())
                    .filter(m -> m.getBrand() == brand)
                    .toArray(EModel[]::new);
            throw new IllegalArgumentException("Invalid model for brand: " + brand +
                    ". Following" + brand + "models are present: " + Arrays.toString(fordModels));
        }
    }

    private void validateEnums() {
        if (!EnumUtils.isValidEnum(EBrand.class, brand.name())) {
            throw new IllegalArgumentException("Available brands: " + Arrays.toString(EBrand.values()));
        }
    }


}