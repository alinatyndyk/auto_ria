package com.example.auto_ria.models;

import com.example.auto_ria.enums.ERegion;
import com.example.auto_ria.enums.ERole;
import com.example.auto_ria.enums.ESeller;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;


@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CompanySQL extends Seller {

    @Size(max = 255, message = "desc must have less than 255 characters")
//    @JsonView({ViewsUser.SL1.class, ViewsUser.NoSL.class})
    private String description = null;

//    private List<String> refreshTokens = new ArrayList<>(); // todo multiple refresh tokens


    @Builder
    public CompanySQL(String name, String city, ERegion region, ERole role, String email, String number, String avatar, String password, String description, ESeller sellerType) {
        super(name, city, region, role, email, number, avatar, password, sellerType);
        this.description = description;
    }
}
