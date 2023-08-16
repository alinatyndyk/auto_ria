package com.example.auto_ria.dto;

import com.example.auto_ria.enums.EBrand;
import com.example.auto_ria.enums.ECurrency;
import com.example.auto_ria.enums.EModel;
import com.example.auto_ria.enums.ERegion;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CarDTO {


    @NotBlank(message = "brand cant be empty")
    @Size(min = 3, message = "brand must have more than 3 characters")
    @Size(max = 20, message = "name must have less than 20 characters")
    private EBrand brand;

    @NotBlank(message = "model cant be empty")
    private EModel model;

    @NotBlank(message = "power cant be empty")
    @Min(value = 200, message = "Power has to be more than 200")
    @Max(value = 3000, message = "Power has to be less than 3000")
    private int powerH;

    @NotBlank(message = "city cant be empty")
    @Size(min = 2, message = "City has to be more than 2")
    @Size(max = 20, message = "City has to be less than 20")
    private String city;

    @NotBlank(message = "region cant be empty")
    private ERegion region;

    @NotBlank(message = "price cant be empty")
    private String price;

    @NotBlank(message = "currency cant be empty")
    private ECurrency currency;
    private List<String> photo;

    private String description;

    private boolean isActivated;

}
