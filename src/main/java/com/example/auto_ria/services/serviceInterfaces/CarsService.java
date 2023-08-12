package com.example.auto_ria.services.serviceInterfaces;

import com.example.auto_ria.dto.CarDTO;
import com.example.auto_ria.dto.updateDTO.CarUpdateDTO;
import com.example.auto_ria.models.AdministratorSQL;
import com.example.auto_ria.models.CarSQL;
import com.example.auto_ria.models.SellerSQL;
import com.example.auto_ria.models.responses.ErrorResponse;
import io.jsonwebtoken.io.IOException;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface CarsService {

    ResponseEntity<List<CarSQL>> getAll();

    ResponseEntity<CarSQL> getById(int id);

    ResponseEntity<String> deleteById(int id, SellerSQL seller, AdministratorSQL administratorSQL) throws ErrorResponse;

    ResponseEntity<CarSQL> post(CarDTO carDTO, SellerSQL seller);
    ResponseEntity<CarSQL> update(int id, CarUpdateDTO carDTO, SellerSQL seller) throws IllegalAccessException, IOException, ErrorResponse, NoSuchFieldException;


}
