package com.example.auto_ria.models.responses.user;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CustomerResponse {

    private int id;
    private String name;
    private String lastName;
    private String city;
    private String region;
    private String avatar;
    private LocalDateTime createdAt;
}
