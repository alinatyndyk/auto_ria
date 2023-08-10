package com.example.auto_ria.dto.updateDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ManagerUpdateDTO {

    private String name;
    private String lastName;

}
