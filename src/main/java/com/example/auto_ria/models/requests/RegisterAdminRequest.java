package com.example.auto_ria.models.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterAdminRequest {
    private String name;
    private String lastName;
    private String email;
    private String avatar = null;
    private String password;
}
