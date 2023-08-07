package com.example.auto_ria.dto;

import com.example.auto_ria.enums.ERegion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

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
    private String photo; //todo album

}
