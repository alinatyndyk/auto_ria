package com.example.auto_ria.dto;

import com.example.auto_ria.enums.EBrand;
import com.example.auto_ria.enums.ECurrency;
import com.example.auto_ria.enums.EModel;
import com.example.auto_ria.enums.ERegion;
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

    private EBrand brand;
    private EModel model;
    private int powerH;
    private String city;
    private ERegion region;
    private String price;
    private ECurrency currency;
    private List<String> photo; //todo album
    private String description; //todo album
    private boolean isActivated;

}
