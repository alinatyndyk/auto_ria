package com.example.auto_ria.dto;

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

    private String brand;
    private int powerH;
    private String city;
    private ERegion region;
    private String producer;
    private String price;
    private List<String> photo; //todo album

}
