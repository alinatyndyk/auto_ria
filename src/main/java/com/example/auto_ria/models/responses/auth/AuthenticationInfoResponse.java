package com.example.auto_ria.models.responses.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthenticationInfoResponse {
    private String accessToken;
    private String refreshToken;
    private int id;
}
