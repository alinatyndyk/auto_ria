package com.example.auto_ria.models.responses.user;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ManagerResponse {

    private int id;
    private String name;
    private String lastName;
    private String avatar;
    private LocalDateTime createdAt;
}
