package com.example.auto_ria.models;

import com.example.auto_ria.enums.ERegion;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @JsonView(ViewsCar.SL1.class)
    private int id;

    //    @NotBlank(message = "brand is required")
//    @Size(min = 3, message = "brand must have more than 2 characters")
//    @Size(max = 255, message = "name must have less than 255 characters")
//    @JsonView({ViewsCar.SL1.class, ViewsCar.SL2.class})
    private String brand;

    //    @NotNull(message = "power is required")
//    @Min(value = 200, message = "Power has to be more than 200")
//    @Max(value = 3000, message = "Power has to be less than 3000")
//    @JsonView({ViewsCar.SL1.class, ViewsCar.SL2.class})
    private Integer powerH;

    //    @NotBlank(message = "brand is required")
//    @Min(value = 2, message = "City has to be more than 2")
//    @Max(value = 20, message = "City has to be less than 20")
    private String city; //todo search

    @Enumerated(EnumType.STRING)
    private ERegion region;

    //    @JsonView({ViewsCar.SL1.class, ViewsCar.SL2.class, ViewsCar.SL3.class})
    private String producer; //todo ecountry

    private String photo;

//    @ElementCollection
//    private List<String> album = new ArrayList<>();

    @JsonManagedReference
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinTable(
            name = "seller_cars",
            joinColumns = @JoinColumn(name = "car_id"),
            inverseJoinColumns = @JoinColumn(name = "seller_id")
    )
    private SellerSQL seller;

    private String price; //todo currency

    public Car(String brand, Integer powerH, String city, ERegion region, String producer, String photo, SellerSQL seller, String price) {
        this.brand = brand;
        this.powerH = powerH;
        this.city = city;
        this.region = region;
        this.producer = producer;
        this.photo = photo;
        this.seller = seller;
        this.price = price;
    }
}