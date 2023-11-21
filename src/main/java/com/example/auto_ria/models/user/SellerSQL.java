package com.example.auto_ria.models.user;

import com.example.auto_ria.enums.EAccountType;
import com.example.auto_ria.enums.ERole;
import com.example.auto_ria.models.CarSQL;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SellerSQL extends Person {

    private String city;
    private String region;
    private String number;

    private String paymentSource;
    private boolean isPaymentSourcePresent;

    private Boolean hasDefaultCard;

    @JsonBackReference
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "seller_cars",
            joinColumns = @JoinColumn(name = "seller_id"),
            inverseJoinColumns = @JoinColumn(name = "car_id")
    )
    private List<CarSQL> cars = new ArrayList<>();

    @ElementCollection
    private List<String> sessions =  new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private EAccountType accountType = EAccountType.BASIC;

    @Builder(builderMethodName = "sellerBuilder")
    public SellerSQL(
            String name,
            String email,
            String avatar,
            String password,
            List<ERole> roles,
            String city,
            String region,
            String number,
            String lastName) {

        super(name, lastName, email, avatar, password, roles);
        this.city = city;
        this.region = region;
        this.number = number;
    }

    @Builder(builderMethodName = "adminBuilder")
    public SellerSQL(
            @JsonProperty("name") String name,
            @JsonProperty("region") String region,
            @JsonProperty("city") String city,
            @JsonProperty("number") String number,
            @JsonProperty("roles") List<ERole> roles,
            @JsonProperty("id") int id,
            @JsonProperty("lastName") String lastName
    ) {
        super(id, name, lastName, roles);
        this.number = number;
        this.region = region;
        this.city = city;
    }
}
