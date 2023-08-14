package com.example.auto_ria.models;

import com.example.auto_ria.enums.EBrand;
import com.example.auto_ria.enums.ECurrency;
import com.example.auto_ria.enums.EModel;
import com.example.auto_ria.enums.ERegion;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarSQL {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @JsonView(ViewsCar.SL1.class)
    private int id;

    //    @NotBlank(message = "brand is required")
//    @Size(min = 3, message = "brand must have more than 2 characters")
//    @Size(max = 255, message = "name must have less than 255 characters")
//    @JsonView({ViewsCar.SL1.class, ViewsCar.SL2.class})
    private EBrand brand;

    //    @NotNull(message = "power is required")
//    @Min(value = 200, message = "Power has to be more than 200")
//    @Max(value = 3000, message = "Power has to be less than 3000")
//    @JsonView({ViewsCar.SL1.class, ViewsCar.SL2.class})
    private Integer powerH;

    //    @NotBlank(message = "brand is required")
//    @Min(value = 2, message = "City has to be more than 2")
//    @Max(value = 20, message = "City has to be less than 20")
    private String city;

    @Enumerated(EnumType.STRING)
    private ERegion region;

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
    public CarSQL(EBrand brand, Integer powerH, String city, ERegion region,
                  EModel model, List<String> photo, SellerSQL seller, String price,
                  ECurrency currency, String description, boolean isActivated) {
        this.brand = brand;
        this.model = model;
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
            throw new IllegalArgumentException("Invalid model for brand: " + brand);
        }
    }
}