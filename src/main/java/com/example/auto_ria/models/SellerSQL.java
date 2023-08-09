package com.example.auto_ria.models;

import com.example.auto_ria.enums.EAccountType;
import com.example.auto_ria.enums.ERegion;
import com.example.auto_ria.enums.ERole;
import com.fasterxml.jackson.annotation.JsonBackReference;
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

    private String city; //todo search

    @Enumerated(EnumType.STRING)
    private ERegion region;

    @Column(unique = true) // todo add regex expressions
//    @JsonView({ViewsUser.SL1.class, ViewsUser.NoSL.class})
    private String number;


    @JsonBackReference
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "seller_cars",
            joinColumns = @JoinColumn(name = "seller_id"),
            inverseJoinColumns = @JoinColumn(name = "car_id")
    )
    private List<Car> cars = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private EAccountType accountType = EAccountType.BASIC;

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
}
