package com.example.auto_ria.models.responses.user;

import com.example.auto_ria.enums.EAccountType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SellerCarResponse {

    private int id;
    private String name;
    private String lastName;
    private String city;
    private String region;
    private String number;
    private String avatar;
    private LocalDateTime createdAt;
}
