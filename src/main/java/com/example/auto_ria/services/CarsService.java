package com.example.auto_ria.services;

import com.example.auto_ria.dao.CarDAO;
import com.example.auto_ria.dto.CarDTO;
import com.example.auto_ria.dto.CarUpdateDTO;
import com.example.auto_ria.models.Car;
import com.example.auto_ria.models.Seller;
import com.example.auto_ria.models.responses.ErrorResponse;
import io.jsonwebtoken.io.IOException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CarsService {

    private CarDAO carDAO;

    public ResponseEntity<List<Car>> getAll() {
        HttpHeaders httpHeaders = new HttpHeaders();
        return new ResponseEntity<>(carDAO.findAll(), httpHeaders, HttpStatus.ACCEPTED);
    }

    public ResponseEntity<Car> getById(int id) {
        return new ResponseEntity<>(carDAO.findById(id).get(), HttpStatus.ACCEPTED);
    }

    public ResponseEntity<Car> post(CarDTO carDTO, Seller seller) {

        Car car = Car.builder()
                .brand(carDTO.getBrand())
                .powerH(carDTO.getPowerH())
                .city(carDTO.getCity())
                .region(carDTO.getRegion())
                .producer(carDTO.getProducer())
                .price(carDTO.getPrice())
                .photo(carDTO.getPhoto())
                .seller(seller)
                .build();
        return new ResponseEntity<>(carDAO.save(car), HttpStatus.ACCEPTED);
    }

    public ResponseEntity<List<Car>> deleteById(int id, Seller seller) throws ErrorResponse {
        Car car = carDAO.findById(id).get();
        if (!doesBelongToSeller(seller, car)) {
            throw new ErrorResponse(403, "Error.Delete_fail: The car does not belong to seller");
        }
        return new ResponseEntity<>(carDAO.findAll(), HttpStatus.GONE);
    }

    public ResponseEntity<List<Car>> getByBrand(String brand) {
        return new ResponseEntity<>(carDAO.findByBrand(brand), HttpStatus.ACCEPTED);
    }

    public ResponseEntity<List<Car>> getByPower(int power) {
        return new ResponseEntity<>(carDAO.findByPowerH(power), HttpStatus.ACCEPTED);
    }

    public boolean doesBelongToSeller(Seller seller, Car car) {
        return seller.getId() == car.getSeller().getId();
    }

    //todo separate update for avatar
    public ResponseEntity<Car> update(int id, CarUpdateDTO carDTO, Seller seller) throws IllegalAccessException, IOException, ErrorResponse, NoSuchFieldException {

        Car car = getById(id).getBody();

        assert car != null;
        if (doesBelongToSeller(seller, car)) {

            Class<?> carDTOClass = carDTO.getClass();
            Field[] fields = carDTOClass.getDeclaredFields();

            for (Field field : fields) {

                field.setAccessible(true);

                String fieldName = field.getName();
                Object fieldValue = field.get(carDTO);

                if (fieldValue != null) {

                    Field carField = Car.class.getDeclaredField(fieldName);

                    carField.setAccessible(true);
                    carField.set(car, fieldValue);
                }
            }
        } else {
            throw new ErrorResponse(403, "Error.Update_fail: The car does not belong to seller");  //todo normal error
        }
        return new ResponseEntity<>(carDAO.save(car), HttpStatus.ACCEPTED);
    }


}
