package com.example.auto_ria.models.responses.user;

import com.example.auto_ria.enums.EAccountType;
import com.example.auto_ria.enums.ERole;
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
    private ERole role;
    private String avatar;
    private LocalDateTime createdAt;
}
