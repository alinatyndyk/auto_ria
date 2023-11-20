package com.example.auto_ria.models.user;

import com.example.auto_ria.enums.ERole;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor

public class CustomerSQL extends Person {

    private String region;
    private String city;

    @ElementCollection
    private List<String> sessions = new ArrayList<>();

    @Builder(builderMethodName = "customerBuilder")
    public CustomerSQL(String name, String lastName, String email, String avatar, String password, List<ERole> roles,
                       String region, String city) {
        super(name, lastName, email, avatar, password, roles);
        this.region = region;
        this.city = city;
    }
}
