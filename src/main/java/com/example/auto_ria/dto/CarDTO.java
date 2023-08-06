package com.example.auto_ria.dto;

import com.example.auto_ria.enums.ERegion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CarDTO {

    private String brand;
    private int power;
    private String city;
    private ERegion region;
    private String producer;
    private String price;
    private String photo; //todo album

}
