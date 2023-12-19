package com.example.auto_ria.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdministratorDTO {

    private String name;
    private String lastName;
    private String email;
    private String avatar;
    private String password;

}
