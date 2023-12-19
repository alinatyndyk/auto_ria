package com.example.auto_ria.dto.updateDTO;

import com.example.auto_ria.enums.ECurrency;
import jakarta.validation.constraints.Max;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CarUpdateDTO {

    private String city;

    private String region;

    @Max(value = 100000000, message = "price has to be less than 100 000 000")
    private String price;

    private ECurrency currency;

    private String description;

}