package com.example.auto_ria.models.responses;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SellerResponse {

    private int id;
    private String name;
    private String city;
    private String region;
    private String number;
    private String avatar;
    private LocalDateTime createdAt;
}
