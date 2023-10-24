package com.example.auto_ria.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthSQL {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int personId;

    private String accessToken;
    private String refreshToken;

}
