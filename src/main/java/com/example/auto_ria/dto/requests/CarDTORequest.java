package com.example.auto_ria.dto.requests;

import com.example.auto_ria.enums.EBrand;
import com.example.auto_ria.enums.ECurrency;
import com.example.auto_ria.enums.EModel;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarDTORequest {

    private EBrand brand;

    private EModel model;

    @Min(value = 200, message = "Power has to be more than 200")
    @Max(value = 3000, message = "Power has to be less than 3000")
    private int powerH;

    @NotEmpty(message = "City cannot be empty")
    private String city;

    @NotEmpty(message = "Region cannot be empty")
    private String region;

    @Max(value = 100000000, message = "price has to be less than 100 000 000")
    private String price;

    private ECurrency currency;

    @NotEmpty(message = "Description cannot be empty")
    private String description;

}
