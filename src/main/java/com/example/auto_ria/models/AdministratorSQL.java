package com.example.auto_ria.models;

import com.example.auto_ria.enums.ERole;
import jakarta.persistence.Entity;
import lombok.*;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdministratorSQL extends Person {  // todo add admin when api starts

    private String lastName;

    @Builder(builderMethodName = "adminBuilder")
    public AdministratorSQL(String name, String lastName, String email, String avatar, String password, List<ERole> roles) {
        super(name, email, avatar, password, roles);
        this.lastName = lastName;
    }

}