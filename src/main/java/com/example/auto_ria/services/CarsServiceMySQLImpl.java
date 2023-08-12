package com.example.auto_ria.services;

import com.example.auto_ria.dao.CarDaoSQL;
import com.example.auto_ria.dto.CarDTO;
import com.example.auto_ria.dto.updateDTO.CarUpdateDTO;
import com.example.auto_ria.enums.EMail;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.mail.FMService;
import com.example.auto_ria.models.AdministratorSQL;
import com.example.auto_ria.models.CarSQL;
import com.example.auto_ria.models.SellerSQL;
import freemarker.template.TemplateException;
import io.jsonwebtoken.io.IOException;
import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

@Service
@AllArgsConstructor
public class CarsServiceMySQLImpl {

    private CarDaoSQL carDAO;
    private FMService mailer;

    public ResponseEntity<List<CarSQL>> getAll() {
        HttpHeaders httpHeaders = new HttpHeaders();
        return new ResponseEntity<>(carDAO.findAll(), httpHeaders, HttpStatus.ACCEPTED);
    }

    public ResponseEntity<CarSQL> getById(int id) {
        assert carDAO.findById(id).isEmpty();
        return new ResponseEntity<>(carDAO.findById(id).get(), HttpStatus.ACCEPTED);
    }

    public CarSQL extractById(int id) {
        assert carDAO.findById(id).isEmpty();
        return carDAO.findById(id).get();
    }

    public ResponseEntity<List<CarSQL>> getBySeller(SellerSQL seller) {
        return new ResponseEntity<>(carDAO.findBySeller(seller), HttpStatus.ACCEPTED);
    }

    public List<CarSQL> getBySellerList(SellerSQL seller) {
        return carDAO.findBySeller(seller);
    }

    public ResponseEntity<CarSQL> post(CarDTO carDTO, SellerSQL seller, boolean status) throws MessagingException, TemplateException, java.io.IOException {

        CarSQL car = CarSQL.builder()
                .brand(carDTO.getBrand())
                .powerH(carDTO.getPowerH())
                .city(carDTO.getCity())
                .region(carDTO.getRegion())
                .producer(carDTO.getProducer())
                .price(carDTO.getPrice())
                .currency(carDTO.getCurrency())
                .photo(carDTO.getPhoto())
                .seller(seller)
                .build();

        CarSQL carSQL = carDAO.save(car);

        if (!status) {
            HashMap<String, Object> vars = new HashMap<>();
            vars.put("car_id", carSQL.getId());

            mailer.sendEmail(seller.getEmail(), EMail.CAR_BEING_CHECKED, vars);
        }

        return new ResponseEntity<>(carSQL, HttpStatus.ACCEPTED);
    }

    public ResponseEntity<String> deleteById(int id, SellerSQL seller, AdministratorSQL administratorSQL) {
        assert carDAO.findById(id).isEmpty();
        CarSQL car = carDAO.findById(id).get();
        assert administratorSQL != null;
        if (!doesBelongToSeller(seller, car)) {
            throw new CustomException("Error.Delete_fail: The car does not belong to seller", HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>("Success.Car_deleted", HttpStatus.GONE);
    }

    public boolean doesBelongToSeller(SellerSQL seller, CarSQL car) {
        return seller.getId() == car.getSeller().getId();
    }

    public ResponseEntity<CarSQL> update(int id, CarUpdateDTO carDTO, SellerSQL seller) throws IllegalAccessException,
            IOException, NoSuchFieldException {

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
            throw new CustomException("Error.Update_fail: The car does not belong to seller", HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(carDAO.save(car), HttpStatus.ACCEPTED);
    }


}
