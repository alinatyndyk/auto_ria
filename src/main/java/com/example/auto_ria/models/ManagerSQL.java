package com.example.auto_ria.models;

import com.example.auto_ria.enums.ERole;
import jakarta.persistence.Entity;
import lombok.*;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@NoArgsConstructor
@ToString
public class ManagerSQL extends Person {

    @Builder(builderMethodName = "managerSQLBuilder")
    public ManagerSQL(String name, String email, String avatar, String password, List<ERole> roles, String lastName) {
        super(name, lastName, email, avatar, password, roles);
    }

    @Builder(builderMethodName = "managerSQLBuilderUpload")
    public ManagerSQL(String email, String password, List<ERole> roles) {
        super(email, password, roles);
    }

}
