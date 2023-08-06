package com.example.auto_ria.models;

import com.example.auto_ria.enums.ERegion;
import com.example.auto_ria.enums.ERole;
import com.example.auto_ria.enums.ESeller;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserSQL extends Seller {

    @NotBlank(message = "last name is required")
    @Size(min = 3, message = "last name must have more than 3 characters")
    @Size(max = 255, message = "last name must have less than 255 characters")
//    @JsonView({ViewsUser.SL1.class, ViewsUser.NoSL.class})
    private String lastName;

    @Builder
    public UserSQL(String name, String city, ERegion region, ERole role, String email, String number, String avatar, String password, String lastName, ESeller sellerType) {
        super(name, city, region, role, email, number, avatar, password, sellerType);
        this.lastName = lastName;
    }
}
