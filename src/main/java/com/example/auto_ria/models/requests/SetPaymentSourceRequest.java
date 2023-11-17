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
public class SetPaymentSourceRequest { //todo dto

    private String id;
    private String token;

    private boolean useDefaultCard;
    private boolean setAsDefaultCard;
    private boolean autoPay;
}
