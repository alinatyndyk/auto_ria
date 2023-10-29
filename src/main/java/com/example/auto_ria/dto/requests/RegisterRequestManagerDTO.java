package com.example.auto_ria.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterRequestManagerDTO {

    @NotEmpty(message = "name cant be empty")
    @Size(min = 2, message = "name must have more than 2 characters")
    @Size(max = 20, message = "name must have less than 20 characters")
    private String name;

    @NotEmpty(message = "last name cant be empty")
    @Size(min = 2, message = "last name must have more than 2 characters")
    @Size(max = 20, message = "last name must have less than 20 characters")
    private String lastName;

    @NotBlank(message = "email cant be empty")
    @Pattern(regexp = "^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$", message = "Invalid email")
    private String email;

    @NotBlank(message = "password cant be empty")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
            message = "Invalid password. Must contain: uppercase letter, lowercase letter, number, special character. " +
                    "At least 8 characters long")
    private String password;
}
