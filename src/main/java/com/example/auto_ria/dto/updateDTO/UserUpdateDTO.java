package com.example.auto_ria.dto.updateDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserUpdateDTO {

    private String name;
    private String lastName;
    private String city;
    private String region;
    private String number;

}
