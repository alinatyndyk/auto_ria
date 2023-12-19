package com.example.auto_ria.models.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginRequest {

    @NotBlank(message = "email cant be empty")
    private String email;

    @NotBlank(message = "password cant be empty")
    private String password;

}
