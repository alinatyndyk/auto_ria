package com.example.auto_ria.models.responses.user;

import com.example.auto_ria.enums.EAccountType;
import com.example.auto_ria.enums.ERole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {

    private int id;
    private String name;
    private String lastName;
    private String city;
    private String region;
    private String number;
    private String avatar;
    private EAccountType accountType;
    private ERole role;
    private boolean isPaymentSourcePresent;

    private LocalDateTime lastOnline;
    private LocalDateTime createdAt;
}
