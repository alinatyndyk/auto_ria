package com.example.auto_ria.models.requests;

import com.example.auto_ria.enums.ERegion;
import com.example.auto_ria.enums.ESeller;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterRequest {
    private String name;
    private String lastName = null;
    private String city;
    private ERegion region;
    private String email;
    private String number;
    private String avatar = null;
    private ESeller sellerType;

    private String password;
}
