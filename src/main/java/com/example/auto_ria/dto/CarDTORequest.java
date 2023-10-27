package com.example.auto_ria.dto;

import com.example.auto_ria.enums.EBrand;
import com.example.auto_ria.enums.ECurrency;
import com.example.auto_ria.enums.EModel;
import com.example.auto_ria.enums.ERegion;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
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

    @Size(min = 2, message = "City has to be more than 2")
    @Size(max = 20, message = "City has to be less than 20")
    private String city;

    private ERegion region;

    @Min(value = 100000000, message = "price has to be less than 100 000 000")
    private String price;

    private ECurrency currency;

    private String description;

}
