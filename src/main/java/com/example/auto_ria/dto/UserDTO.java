package com.example.auto_ria.dto;

import com.example.auto_ria.enums.ERegion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    private String city;
    private ERegion region;
    private String number;
    private String name;
    private String lastName;
    private String email;
    private String avatar;
    private String password;

}
