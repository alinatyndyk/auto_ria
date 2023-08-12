package com.example.auto_ria.dto.updateDTO;

import com.example.auto_ria.enums.ECurrency;
import com.example.auto_ria.enums.ERegion;
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
    private ERegion region;
    private String price;
    private ECurrency currency;

}