package com.example.auto_ria.services;

import com.example.auto_ria.dao.CarDaoSQL;
import com.example.auto_ria.dto.CarDTO;
import com.example.auto_ria.dto.updateDTO.CarUpdateDTO;
import com.example.auto_ria.enums.*;
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

    public ResponseEntity<MiddlePriceResponse> getMiddlePrice(EBrand brand, ERegion region) {

        double totalInUSD = 0.00;
        double totalInUAH = 0.00;
        double totalInEUR = 0.00;

        double middleInUSD = 0.00;
        double middleInUAH = 0.00;
        double middleInEUR = 0.00;

        HashMap<String, Object> params = new HashMap<>();
        params.put("brand", brand);
        params.put("region", region);

        List<Map<String, Object>> pricesAndCurrencies = carDAO.findPricesByBrandAndRegion(params);
        System.out.println(pricesAndCurrencies);
        System.out.println("pricesAndCurrencies");

        for (Map<String, Object> map : pricesAndCurrencies) {
            String currency = map.get("currency").toString();
            String price = map.get("price").toString();

            CurrencyConverterResponse response =
                    currencyConverterService.convert(ECurrency.valueOf(currency), price);

            totalInUAH = totalInUAH + response.getCurrencyHashMap().get(ECurrency.UAH);
            totalInEUR = totalInEUR + response.getCurrencyHashMap().get(ECurrency.EUR);
            totalInUSD = totalInUSD + response.getCurrencyHashMap().get(ECurrency.USD);

            middleInUAH = totalInUAH / carDAO.countByBrandAndRegion(brand, region);
            middleInUSD = totalInUSD / carDAO.countByBrandAndRegion(brand, region);
            middleInEUR = totalInEUR / carDAO.countByBrandAndRegion(brand, region);

        }

        return ResponseEntity.ok(MiddlePriceResponse.builder()
                .middleInUAH(middleInUAH)
                .middleInEUR(middleInEUR)
                .middleInUSD(middleInUSD)
                .build());
    }

    public ResponseEntity<Page<CarResponse>> getAll(int page, CarSQL params) {
        ExampleMatcher matcher = ExampleMatcher.matchingAll()
                .withIgnoreNullValues()
                .withIgnorePaths("id")
                .withIgnorePaths("photo")
                .withIgnorePaths("isActivated");

        Example<CarSQL> example = Example.of(params, matcher);

        Pageable pageable = PageRequest.of(page, 2);
        Page<CarSQL> carsPage = carDAO.findAll(example, pageable);

        Page<CarResponse> carResponsesPage = carsPage.map(this::formCarResponse);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(carResponsesPage);
    }

    public ResponseEntity<CarResponse> getById(int id, HttpServletRequest request) {
        assert carDAO.findById(id).isPresent();
        CarSQL carSQL = carDAO.findById(id).get();

        SellerSQL sellerSQL = usersServiceMySQL.extractSellerFromHeader(request);
        ManagerSQL managerSQL = usersServiceMySQL.extractManagerFromHeader(request);
        AdministratorSQL administratorSQL = usersServiceMySQL.extractAdminFromHeader(request);

        if (!carSQL.isActivated()) {
            if (managerSQL == null
                    && administratorSQL == null) {
                throw new CustomException("The announcement is not activated", HttpStatus.FORBIDDEN);
            }
        }

        CarResponse carResponse = formCarResponse(carSQL);

        if (managerSQL != null || administratorSQL != null || sellerSQL != null) {
            if (sellerSQL != null && sellerSQL.getAccountType().equals(EAccountType.PREMIUM)) {
                MiddlePriceResponse response = getMiddlePrice(carSQL.getBrand(), carSQL.getRegion()).getBody();
                assert response != null;
                carResponse.setMiddlePriceUAH(response.getMiddleInUAH());
                carResponse.setMiddlePriceUAH(response.getMiddleInEUR());
                carResponse.setMiddlePriceUAH(response.getMiddleInUSD());
            }
        }

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

    public ResponseEntity<String> ban(int id) {
        CarSQL carSQL = extractById(id);
        carSQL.setActivated(false);
        carDAO.save(carSQL);
        try {
            HashMap<String, Object> vars = new HashMap<>();
            vars.put("name", carSQL.getSeller().getName());
            vars.put("car_id", carSQL.getId());
            mailer.sendEmail(carSQL.getSeller().getEmail(), EMail.CAR_BEING_BANNED, vars);
        } catch (Exception ignore) {
        }
        return ResponseEntity.ok("Car banned successfully");
    }

    public ResponseEntity<Page<CarResponse>> getBySeller(SellerSQL seller, int page) {
        Pageable pageable = PageRequest.of(page, 2);

        Page<CarSQL> carsPage = carDAO.findAllBySeller(seller, pageable);
        System.out.println("jvhyasjnagsvd");

        Page<CarResponse> carResponsesPage = carsPage.map(carSQL -> {
            CarResponse carResponse = formCarResponse(carSQL);
        System.out.println("zzzzzzzzzzzzzz");

            if (seller.equals(carSQL.getSeller()) && seller.getAccountType().equals(EAccountType.PREMIUM)) {
                MiddlePriceResponse response = getMiddlePrice(carSQL.getBrand(), carSQL.getRegion()).getBody();

                assert response != null;
                carResponse.setMiddlePriceUAH(response.getMiddleInUAH());
                carResponse.setMiddlePriceUAH(response.getMiddleInEUR());
                carResponse.setMiddlePriceUAH(response.getMiddleInUSD());
            }

            return carResponse;

        });

        return new ResponseEntity<>(carResponsesPage, HttpStatus.ACCEPTED);
    }

    public List<CarSQL> findAllBySeller(SellerSQL seller) {
        return carDAO.findBySeller(seller);
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

    public ResponseEntity<CarResponse> update(int id, CarUpdateDTO carDTO, SellerSQL seller) throws IllegalAccessException,
            IOException, NoSuchFieldException {

        CarSQL car = extractById(id);
        CarResponse carResponse;

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

            CarSQL carSQL = carDAO.save(car);

            carResponse = formCarResponse(carSQL);

            if (seller.getAccountType().equals(EAccountType.PREMIUM)) {
                MiddlePriceResponse response = getMiddlePrice(carSQL.getBrand(), carSQL.getRegion()).getBody();

                assert response != null;
                carResponse.setMiddlePriceUAH(response.getMiddleInUAH());
                carResponse.setMiddlePriceUAH(response.getMiddleInEUR());
                carResponse.setMiddlePriceUAH(response.getMiddleInUSD());
            }

        } else {
            throw new CustomException("Error.Update_fail: The car does not belong to seller", HttpStatus.FORBIDDEN);
        }
        return ResponseEntity.ok(carResponse);
    }

    private CarResponse formCarResponse(CarSQL carSQL) {

        CurrencyConverterResponse converterResponse = currencyConverterService.convert(carSQL.getCurrency(), carSQL.getPrice());

        return CarResponse.builder()
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
                .build();
    }


}
