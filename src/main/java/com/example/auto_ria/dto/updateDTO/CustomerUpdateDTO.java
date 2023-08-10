package com.example.auto_ria.dto.updateDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerUpdateDTO { // todo manager dto customer dto

    private String name;
    private String lastName;
    private String email;
    private String avatar;
    private String password;

}
