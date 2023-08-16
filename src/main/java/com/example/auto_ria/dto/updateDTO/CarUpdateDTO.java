package com.example.auto_ria.dto.updateDTO;

import com.example.auto_ria.enums.ECurrency;
import com.example.auto_ria.enums.ERegion;
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
public class CarUpdateDTO {

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

}