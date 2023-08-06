package com.example.auto_ria.services;

import com.example.auto_ria.dao.CarDAO;
import com.example.auto_ria.dto.CarDTO;
import com.example.auto_ria.dto.CarUpdateDTO;
import com.example.auto_ria.models.Car;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.List;

@Service
@AllArgsConstructor
public class CarsService {

    private CarDAO carDAO;

    public ResponseEntity<List<Car>> getAll() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("access_token", "hjds76sd767636733267");
        return new ResponseEntity<>(carDAO.findAll(), httpHeaders, HttpStatus.ACCEPTED);
    }

    public ResponseEntity<Car> getById(int id) {
        return new ResponseEntity<>(carDAO.findById(id).get(), HttpStatus.ACCEPTED);
    }

    public ResponseEntity<Car> post(CarDTO carDTO) {
        Car car = Car.builder()
                .brand(carDTO.getBrand())
                .power(carDTO.getPower())
                .city(carDTO.getCity())
                .region(carDTO.getRegion())
                .producer(carDTO.getProducer())
                .price(carDTO.getPrice())
                .photo(carDTO.getPhoto())
                .build();

        return new ResponseEntity<>(car, HttpStatus.ACCEPTED);
    }

    public ResponseEntity<List<Car>> deleteById(int id) {
        carDAO.deleteById(id);
        return new ResponseEntity<>(carDAO.findAll(), HttpStatus.GONE);
    }

    public ResponseEntity<List<Car>> getByBrand(String brand) {
        return new ResponseEntity<>(carDAO.findByBrand(brand), HttpStatus.ACCEPTED);
    }

    public ResponseEntity<List<Car>> getByPower(int power) {
        return new ResponseEntity<>(carDAO.findByPower(power), HttpStatus.ACCEPTED);
    }

    public ResponseEntity<Car> update(int id, CarUpdateDTO carDTO) throws IllegalAccessException {

        Car car = getById(id).getBody();

        Class carClass = car.getClass();
        Field[] fields = carClass.getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object fieldValue = field.get(carDTO);
                if (fieldValue != null) {
                    field.set(car, fieldValue);
                }
            } catch (IllegalAccessException e) {
                throw new IllegalAccessException();
            }
        }
        return new ResponseEntity<>(car, HttpStatus.ACCEPTED);
    }


}
