package com.example.auto_ria.services;

import com.example.auto_ria.dao.CarDaoSQL;
import com.example.auto_ria.dto.CarDTO;
import com.example.auto_ria.dto.updateDTO.CarUpdateDTO;
import com.example.auto_ria.enums.EBrand;
import com.example.auto_ria.enums.ECurrency;
import com.example.auto_ria.enums.EMail;
import com.example.auto_ria.enums.ERegion;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.mail.FMService;
import com.example.auto_ria.models.AdministratorSQL;
import com.example.auto_ria.models.CarSQL;
import com.example.auto_ria.models.ManagerSQL;
import com.example.auto_ria.models.SellerSQL;
import com.example.auto_ria.models.responses.CarResponse;
import com.example.auto_ria.models.responses.CurrencyConverterResponse;
import com.example.auto_ria.models.responses.MiddlePriceResponse;
import com.example.auto_ria.models.responses.SellerResponse;
import io.jsonwebtoken.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class CarsServiceMySQLImpl {

    private CarDaoSQL carDAO;
    private UsersServiceMySQLImpl usersServiceMySQL;
    private FMService mailer;
    private CurrencyConverterService currencyConverterService;


    public ResponseEntity<List<CarSQL>> getAll() {
        HttpHeaders httpHeaders = new HttpHeaders();
        return new ResponseEntity<>(carDAO.findAll(), httpHeaders, HttpStatus.ACCEPTED);
    }

    public ResponseEntity<MiddlePriceResponse> getMiddlePrice(EBrand brand, ERegion region) { //todo by model

        double totalInUSD = 0.00;
        double totalInUAH = 0.00;
        double totalInEUR = 0.00;

        HashMap<String, Object> params = new HashMap<>();
        params.put("brand", brand);
        params.put("region", region);

        List<Map<String, Object>> pricesAndCurrencies = carDAO.findPricesByBrandAndRegion(params);
        System.out.println(pricesAndCurrencies);
        System.out.println("pricesAndCurrencies");

        for (Map<String, Object> map : pricesAndCurrencies) {
            String currency = map.get("currency").toString();
            String price = map.get("price").toString();

        System.out.println("in map");
            CurrencyConverterResponse response =
                    currencyConverterService.convert(ECurrency.valueOf(currency), price);
        System.out.println("converted");

            totalInUAH = totalInUAH + response.getCurrencyHashMap().get(ECurrency.UAH);
            totalInEUR = totalInEUR + response.getCurrencyHashMap().get(ECurrency.EUR);
            totalInUSD = totalInUSD + response.getCurrencyHashMap().get(ECurrency.USD);
        System.out.println("totals");
        }

        return ResponseEntity.ok(MiddlePriceResponse.builder()
                .middleInUAH(totalInUAH / carDAO.countByBrandAndRegion(brand, region))
                .middleInEUR(totalInEUR / carDAO.countByBrandAndRegion(brand, region))
                .middleInUSD(totalInUSD / carDAO.countByBrandAndRegion(brand, region))
                .build());
    }

    public ResponseEntity<Page<CarSQL>> getAll(int page, CarSQL params) {
        ExampleMatcher matcher = ExampleMatcher.matchingAll()
                .withIgnoreNullValues()
                .withIgnorePaths("id")
                .withIgnorePaths("photo")
                .withIgnorePaths("isActivated");

        Example<CarSQL> example = Example.of(params, matcher);

        Pageable pageable = PageRequest.of(page, 2);
        Page<CarSQL> cars = carDAO.findAll(example, pageable);

        return new ResponseEntity<>(cars, HttpStatus.ACCEPTED);
    }

    public ResponseEntity<CarResponse> getById(int id, HttpServletRequest request) {
        assert carDAO.findById(id).isPresent();
        CarSQL carSQL = carDAO.findById(id).get();
        if (!carSQL.isActivated()) {
            if (usersServiceMySQL.extractManagerFromHeader(request) == null
                    && usersServiceMySQL.extractAdminFromHeader(request) == null) {
                throw new CustomException("The announcement is not activated", HttpStatus.FORBIDDEN);
            }
        }
        System.out.println("is activated");

        CurrencyConverterResponse converterResponse =
                currencyConverterService.convert(carSQL.getCurrency(), carSQL.getPrice());

        System.out.println("is converted");
        MiddlePriceResponse response = getMiddlePrice(carSQL.getBrand(), carSQL.getRegion()).getBody();
        System.out.println("is middle");

        assert response != null;
        CarResponse carResponse = CarResponse.builder()
                .brand(carSQL.getBrand())
                .powerH(carSQL.getPowerH())
                .city(carSQL.getCity())
                .region(carSQL.getRegion())
                .model(carSQL.getModel())
                .price(carSQL.getPrice())
                .currency(carSQL.getCurrency())
                .photo(carSQL.getPhoto())
                .seller(SellerResponse.builder()
                        .id(carSQL.getSeller().getId())
                        .name(carSQL.getSeller().getName())
                        .avatar(carSQL.getSeller().getAvatar())
                        .city(carSQL.getSeller().getCity())
                        .region(carSQL.getSeller().getRegion())
                        .number(carSQL.getSeller().getNumber())
                        .createdAt(carSQL.getSeller().getCreatedAt())
                        .build())
                .description(carSQL.getDescription())
                .priceUAH(converterResponse.getCurrencyHashMap().get(ECurrency.UAH))
                .priceUSD(converterResponse.getCurrencyHashMap().get(ECurrency.USD))
                .priceEUR(converterResponse.getCurrencyHashMap().get(ECurrency.EUR))
                .middlePriceEUR(response.getMiddleInEUR())
                .middlePriceUAH(response.getMiddleInUAH())
                .middlePriceUSD(response.getMiddleInUSD())
                .build();
        System.out.println("built");

        return new ResponseEntity<>(carResponse, HttpStatus.ACCEPTED);
    }

    public CarSQL extractById(int id) {
        assert carDAO.findById(id).isPresent();
        return carDAO.findById(id).get();
    }

    public ResponseEntity<String> activate(int id) {
        CarSQL carSQL = extractById(id);
        carSQL.setActivated(true);
        carDAO.save(carSQL);
        try {
            HashMap<String, Object> vars = new HashMap<>();
            vars.put("name", carSQL.getSeller().getName());
            vars.put("car_id", carSQL.getId());
            mailer.sendEmail(carSQL.getSeller().getEmail(), EMail.CAR_BEING_ACTIVATED, vars);
        } catch (Exception ignore) {
        }
        return ResponseEntity.ok("Car activated successfully");
    }

    public ResponseEntity<Page<CarSQL>> getBySeller(SellerSQL seller, int page) {
        Pageable pageable = PageRequest.of(page, 2);
        return new ResponseEntity<>(carDAO.findBySeller(seller, pageable), HttpStatus.ACCEPTED);
    }

    public List<CarSQL> findAllBySeller(SellerSQL seller) {
        return carDAO.findAllBySeller(seller);
    }

    public ResponseEntity<CarSQL> post(CarDTO carDTO, SellerSQL seller) {

        CarSQL car = CarSQL.builder()
                .brand(carDTO.getBrand())
                .powerH(carDTO.getPowerH())
                .city(carDTO.getCity())
                .region(carDTO.getRegion())
                .model(carDTO.getModel())
                .price(carDTO.getPrice())
                .currency(carDTO.getCurrency())
                .photo(carDTO.getPhoto())
                .seller(seller)
                .isActivated(carDTO.isActivated())
                .build();

        CarSQL carSQL = carDAO.save(car);

        return new ResponseEntity<>(carSQL, HttpStatus.ACCEPTED);
    }

    public ResponseEntity<String> deleteById(int id, SellerSQL seller, ManagerSQL manager, AdministratorSQL administrator) {
        assert carDAO.findById(id).isPresent();
        CarSQL car = carDAO.findById(id).get();

        if (manager != null || administrator != null) {
            carDAO.deleteById(id);
            try {
                HashMap<String, Object> vars = new HashMap<>();
                vars.put("name", seller.getName());
                vars.put("description", car.getDescription());

                mailer.sendEmail(seller.getEmail(), EMail.CAR_BEING_BANNED, vars);

                return new ResponseEntity<>("Success.Car_deleted", HttpStatus.GONE);
            } catch (Exception ignore) {
            }
        }

        if (!doesBelongToSeller(seller, car)) {
            throw new CustomException("Error.Delete_fail: The car does not belong to seller", HttpStatus.FORBIDDEN);
        }

        carDAO.deleteById(id);
        return new ResponseEntity<>("Success.Car_deleted", HttpStatus.GONE);
    }

    public boolean doesBelongToSeller(SellerSQL seller, CarSQL car) {
        return seller.getId() == car.getSeller().getId();
    }

    public ResponseEntity<CarSQL> update(int id, CarUpdateDTO carDTO, SellerSQL seller) throws IllegalAccessException,
            IOException, NoSuchFieldException {

        CarSQL car = extractById(id); //todo profanity filter

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
