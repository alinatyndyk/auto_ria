package com.example.auto_ria.services.serviceInterfaces;

import com.example.auto_ria.dto.CarDTO;
import com.example.auto_ria.dto.updateDTO.CarUpdateDTO;
import com.example.auto_ria.models.Car;
import com.example.auto_ria.models.SellerSQL;
import com.example.auto_ria.models.responses.ErrorResponse;
import io.jsonwebtoken.io.IOException;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface CarsService {

    ResponseEntity<List<Car>> getAll();

    ResponseEntity<Car> getById(int id);

    ResponseEntity<String> deleteById(int id, SellerSQL seller) throws ErrorResponse;

    ResponseEntity<Car> post(CarDTO carDTO, SellerSQL seller);
    ResponseEntity<Car> update(int id, CarUpdateDTO carDTO, SellerSQL seller) throws IllegalAccessException, IOException, ErrorResponse, NoSuchFieldException;


}
