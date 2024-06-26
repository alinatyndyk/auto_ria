package com.example.auto_ria.models;

import com.example.auto_ria.enums.EBrand;
import com.example.auto_ria.enums.ECurrency;
import com.example.auto_ria.enums.EModel;
import com.example.auto_ria.models.user.UserSQL;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import org.apache.commons.lang3.EnumUtils;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"user"})
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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinTable(
            name = "seller_cars",
            joinColumns = @JoinColumn(name = "car_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "seller_id")
    )
    private UserSQL user;

    private String price;

    private ECurrency currency;

    private String description;

    private boolean isActivated;

    @Column(updatable = false)
    @CreationTimestamp
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate createdAt;

    @UpdateTimestamp()
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "\"dd/MM/yyyy\"", timezone = "GMT")
    private String updatedAt;


    @Builder
    public CarSQL(EBrand brand, Integer powerH, String city, String region,
                  EModel model, List<String> photo, UserSQL user, String price,
                  ECurrency currency, String description, boolean isActivated) {
        this.brand = brand;
        this.model = model;
        validateEnums();
        validateBrandAndModel();
        this.powerH = powerH;
        this.city = city;
        this.region = region;
        this.photo = photo;
        this.user = user;
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