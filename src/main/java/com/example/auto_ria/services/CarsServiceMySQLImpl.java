package com.example.auto_ria.services;

import com.example.auto_ria.dao.CarDaoSQL;
import com.example.auto_ria.dto.CarDTO;
import com.example.auto_ria.dto.updateDTO.CarUpdateDTO;
import com.example.auto_ria.models.CarSQL;
import com.example.auto_ria.models.SellerSQL;
import com.example.auto_ria.models.responses.ErrorResponse;
import com.example.auto_ria.services.serviceInterfaces.CarsService;
import io.jsonwebtoken.io.IOException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.List;

@Service
@AllArgsConstructor
public class CarsServiceMySQLImpl implements CarsService {

    private CarDaoSQL carDAO;

    public ResponseEntity<List<CarSQL>> getAll() {
        HttpHeaders httpHeaders = new HttpHeaders();
        return new ResponseEntity<>(carDAO.findAll(), httpHeaders, HttpStatus.ACCEPTED);
    }

    public ResponseEntity<CarSQL> getById(int id) {
        return new ResponseEntity<>(carDAO.findById(id).get(), HttpStatus.ACCEPTED);
    }

    public ResponseEntity<List<CarSQL>> getBySeller(SellerSQL seller) {
        return new ResponseEntity<>(carDAO.findBySeller(seller), HttpStatus.ACCEPTED);
    }

    public ResponseEntity<CarSQL> post(CarDTO carDTO, SellerSQL seller) {

        CarSQL car = CarSQL.builder()
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

    public ResponseEntity<String> deleteById(int id, SellerSQL seller) throws ErrorResponse {
        CarSQL car = carDAO.findById(id).get();
        if (!doesBelongToSeller(seller, car)) {
            throw new ErrorResponse(403, "Error.Delete_fail: The car does not belong to seller");
        }
        return new ResponseEntity<>("Success.Car_deleted", HttpStatus.GONE);
    }

    public ResponseEntity<List<CarSQL>> getByBrand(String brand) {
        return new ResponseEntity<>(carDAO.findByBrand(brand), HttpStatus.ACCEPTED);
    }

    public ResponseEntity<List<CarSQL>> getByPower(int power) {
        return new ResponseEntity<>(carDAO.findByPowerH(power), HttpStatus.ACCEPTED);
    }

    public boolean doesBelongToSeller(SellerSQL seller, CarSQL car) {
        return seller.getId() == car.getSeller().getId();
    }

    //todo separate update for avatar
    public ResponseEntity<CarSQL> update(int id, CarUpdateDTO carDTO, SellerSQL seller) throws IllegalAccessException, IOException, ErrorResponse, NoSuchFieldException {

        CarSQL car = getById(id).getBody();

        assert car != null;
        if (doesBelongToSeller(seller, car)) {

            Class<?> carDTOClass = carDTO.getClass();
            Field[] fields = carDTOClass.getDeclaredFields();

            for (Field field : fields) {

                field.setAccessible(true);

                String fieldName = field.getName();
                Object fieldValue = field.get(carDTO);

                if (fieldValue != null) {

                    Field carField = CarSQL.class.getDeclaredField(fieldName);

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
