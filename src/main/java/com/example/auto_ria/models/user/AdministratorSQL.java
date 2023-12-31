package com.example.auto_ria.models.user;

import com.example.auto_ria.enums.ERole;
import jakarta.persistence.Entity;
import lombok.*;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@NoArgsConstructor
public class AdministratorSQL extends Person {

    @Builder(builderMethodName = "adminBuilder")
    public AdministratorSQL(String name, String lastName, String email, String avatar, String password, List<ERole> roles) {
        super(name, lastName, email, avatar, password, roles);
    }

}
