package com.example.auto_ria.models;

import com.example.auto_ria.enums.ERegion;
import com.example.auto_ria.enums.ERole;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSQL extends SellerSQL {

    @NotBlank(message = "last name is required")
    @Size(min = 3, message = "last name must have more than 3 characters")
    @Size(max = 255, message = "last name must have less than 255 characters")
//    @JsonView({ViewsUser.SL1.class, ViewsUser.NoSL.class})
    private String lastName;

    @Builder(builderMethodName = "userSQLBuilder")
    public UserSQL(String name, String email, String avatar, String password, List<ERole> roles, String city, ERegion region, String number, String lastName) {
        super(name, email, avatar, password, roles, city, region, number);
        this.lastName = lastName;
    }
}
