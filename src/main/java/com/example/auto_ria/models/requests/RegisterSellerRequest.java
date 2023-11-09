package com.example.auto_ria.models.requests;

import jakarta.validation.constraints.NotBlank;
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
public class RegisterSellerRequest {
    @NotBlank(message = "city is required")
    private String city;

    @NotBlank(message = "region is required")
    private String region;

    @NotBlank(message = "name is required")
    @Size(min = 3, message = "name must have more than 3 characters")
    @Size(max = 30, message = "name must have less than 30 characters")
    private String name;

    @NotBlank(message = "last name is required")
    @Size(min = 3, message = "last name must have more than 3 characters")
    @Size(max = 30, message = "last name must have less than 30 characters")
    private String lastName;

    @Pattern(regexp = "^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$", message = "Invalid email")
    private String email;

    @Pattern(regexp = "^(\\d{3}[- .]?){2}\\d{4}$", message = "Invalid number")
    private String number;

    private String avatar = null;

    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
            message = "Invalid password. Must contain: uppercase letter, lowercase letter, number, special character. " +
                    "At least 8 characters long")
    private String password;
}
