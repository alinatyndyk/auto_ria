package com.example.auto_ria.models;

import com.example.auto_ria.enums.ERegion;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @JsonView(ViewsCar.SL1.class)
    private int id;

    @NotBlank(message = "brand is required")
    @Size(min = 3, message = "brand must have more than 2 characters")
    @Size(max = 255, message = "name must have less than 255 characters")
//    @JsonView({ViewsCar.SL1.class, ViewsCar.SL2.class})
    private String brand;

    private String photo;

    @NotBlank(message = "power is required")
    @Min(value = 200, message = "Power has to be more than 200")
    @Max(value = 3000, message = "Power has to be less than 3000")
//    @JsonView({ViewsCar.SL1.class, ViewsCar.SL2.class})
    private int power;

    private String city; //todo search

    private ERegion region;

//    @JsonView({ViewsCar.SL1.class, ViewsCar.SL2.class, ViewsCar.SL3.class})
    private String producer;

    @ElementCollection
    private List<String> album = new ArrayList<>();

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinTable(
            name = "seller_cars",
            joinColumns = @JoinColumn(name = "car_id"),
            inverseJoinColumns = @JoinColumn(name = "seller_id")
    )
    private Seller sellerId;

    private String price; //todo currency

    @Builder
    public Car(String brand, int power, String city, ERegion region, String producer, String price, String photo) {
        this.brand = brand;
        this.power = power;
        this.city = city;
        this.region = region;
        this.producer = producer;
        this.price = price;
        this.photo = photo;
    }
}