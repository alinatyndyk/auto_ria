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

    @NotBlank(message = "city cant be empty")
    private String city;

    @NotBlank(message = "region cant be empty")
    private ERegion region;

    @Size(min = 9, message = "number must have less than 9 characters")
    @Size(max = 12, message = "name must have more than 12 characters")
    @Pattern(regexp = "^\\d+$", message = "Invalid number: Must contain only numbers")
    private String number;

    @NotBlank(message = "name cant be empty")
    @Size(min = 2, message = "name must have more than 2 characters")
    @Size(max = 20, message = "name must have less than 20 characters")
    private String name;

    @NotBlank(message = "lastName cant be empty")
    @Size(min = 2, message = "lastName must have more than 2 characters")
    @Size(max = 20, message = "lastName must have less than 20 characters")
    private String lastName;

    @NotBlank(message = "email cant be empty")
    @Pattern(regexp = "^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$", message = "Invalid email")
    private String email;

    private String avatar;

    @NotBlank(message = "password cant be empty")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
            message = "Invalid password. Must contain: uppercase letter, lowercase letter, number, special character. " +
                    "At least 8 characters long")
    private String password;

}
