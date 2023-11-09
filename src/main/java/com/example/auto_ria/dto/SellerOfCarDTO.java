package com.example.auto_ria.dto;

import com.example.auto_ria.enums.ERegion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SellerOfCarDTO {

    private String city;
    private ERegion region;
    private String number;
    private String name;
    private String lastName;
    private String email;

}
