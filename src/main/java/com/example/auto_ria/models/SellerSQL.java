package com.example.auto_ria.models;

import com.example.auto_ria.enums.EAccountType;
import com.example.auto_ria.enums.ERegion;
import com.example.auto_ria.enums.ERole;
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

    @Enumerated(EnumType.STRING)
    private ERegion region;

    @Column(unique = true)
    private String number;


    @JsonBackReference
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "seller_cars",
            joinColumns = @JoinColumn(name = "seller_id"),
            inverseJoinColumns = @JoinColumn(name = "car_id")
    )
    private List<CarSQL> cars = new ArrayList<>();

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
            ERegion region,
            String number) {

        super(name, email, avatar, password, roles);
        this.city = city;
        this.region = region;
        this.number = number;
    }

    @Builder(builderMethodName = "adminBuilder")
    public SellerSQL(
            @JsonProperty("name") String name,
            @JsonProperty("roles") List<ERole> roles,
            @JsonProperty("id") int id
    ) {
        super(id, name, roles);
    }
}
