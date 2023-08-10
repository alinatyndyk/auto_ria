package com.example.auto_ria.models;

import com.example.auto_ria.enums.ERole;
import jakarta.persistence.Entity;
import lombok.*;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ManagerSQL extends Person {

    private String permissions;

    @Builder(builderMethodName = "managerSQLBuilder")
    public ManagerSQL(String name, String email, String avatar, String password, List<ERole> roles, String permissions) {
        super(name, email, avatar, password, roles);
        this.permissions = permissions;
    }

    @Builder(builderMethodName = "managerSQLBuilderUpload")
    public ManagerSQL(String email, String password, List<ERole> roles) {
        super(email, password, roles);
    }

}
