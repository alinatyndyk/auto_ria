package com.example.auto_ria.dto;

import com.example.auto_ria.enums.EBrand;
import com.example.auto_ria.enums.ECurrency;
import com.example.auto_ria.enums.EModel;
import com.example.auto_ria.enums.ERegion;
import com.example.auto_ria.services.CitiesService;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.List;

@Data
@NoArgsConstructor
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
    private String region;

    @NotBlank(message = "price cant be empty")
    private String price;

    @NotBlank(message = "currency cant be empty")
    private ECurrency currency;

    private List<String> photo;

    @NotEmpty(message = "currency cant be empty")
    private String description;

    private boolean isActivated;

    @Builder
    public CarDTO(EBrand brand, EModel model, int powerH, String city, String region, String price, ECurrency currency, List<String> photo, String description, boolean isActivated) {

        this.brand = brand;
        validateEnums();

        this.model = model;
        validateBrandAndModel();

        this.powerH = powerH;
        this.city = city;

        this.region = region;
        this.price = price;
        this.currency = currency;
        this.photo = photo;
        this.description = description;
        this.isActivated = isActivated;
    }



    private void validateBrandAndModel() {
        if (model.getBrand() != brand) {
            EModel[] fordModels = Arrays.stream(EModel.values())
                    .filter(m -> m.getBrand() == brand)
                    .toArray(EModel[]::new);
            throw new IllegalArgumentException("Invalid model for brand: " + brand +
                    ". Following" + brand + "models are present: " + Arrays.toString(fordModels));
        }
    }

    private void validateEnums() {
        if (!EnumUtils.isValidEnum(EBrand.class, brand.name())) {
            throw new IllegalArgumentException("Available brands: " + Arrays.toString(EBrand.values()));
        }
    }
}
