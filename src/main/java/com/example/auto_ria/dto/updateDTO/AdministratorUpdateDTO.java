package com.example.auto_ria.dto.updateDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdministratorUpdateDTO {

    @NotBlank(message = "name cant be empty")
    @Size(min = 2, message = "name must have more than 2 characters")
    @Size(max = 20, message = "name must have less than 20 characters")
    private String name;

    @NotBlank(message = "lastName cant be empty")
    @Size(min = 2, message = "lastName must have more than 2 characters")
    @Size(max = 20, message = "lastName must have less than 20 characters")
    private String lastName;

}
