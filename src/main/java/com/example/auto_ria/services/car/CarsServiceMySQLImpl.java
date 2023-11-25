package com.example.auto_ria.services.car;

import com.example.auto_ria.dao.CarDaoSQL;
import com.example.auto_ria.dto.CarDTO;
import com.example.auto_ria.dto.updateDTO.CarUpdateDTO;
import com.example.auto_ria.enums.*;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.mail.FMService;
import com.example.auto_ria.models.CarSQL;
import com.example.auto_ria.models.responses.car.CarResponse;
import com.example.auto_ria.models.responses.currency.CurrencyConverterResponse;
import com.example.auto_ria.models.responses.car.MiddlePriceResponse;
import com.example.auto_ria.models.responses.user.SellerCarResponse;
import com.example.auto_ria.models.user.AdministratorSQL;
import com.example.auto_ria.models.user.ManagerSQL;
import com.example.auto_ria.models.user.SellerSQL;
import com.example.auto_ria.services.CommonService;
import com.example.auto_ria.services.currency.CurrencyConverterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.core.env.Environment;
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
    private CommonService commonService;
    private FMService mailer;
    private CurrencyConverterService currencyConverterService;

    private Environment environment;


    public ResponseEntity<List<CarSQL>> getAll() {
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            return new ResponseEntity<>(carDAO.findAll(), httpHeaders, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            throw new CustomException("Failed fetch: " + e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    public ResponseEntity<MiddlePriceResponse> getMiddlePrice(EBrand brand, String region) {
        try {

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
        } catch (Exception e) {
            throw new CustomException("Could not get middle price for current item: " + e.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }
    }

    public ResponseEntity<Page<CarResponse>> getAll(int page, CarSQL params) {
        try {
            ExampleMatcher matcher = ExampleMatcher.matchingAll()
                    .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                    .withIgnoreNullValues()
                    .withIgnorePaths("powerH")
                    .withIgnorePaths("id")
                    .withIgnorePaths("photo"); //todo price bigger than - smaller than

            Example<CarSQL> example = Example.of(params, matcher);

            Pageable pageable = PageRequest.of(page, 2);
            Page<CarSQL> carsPage = carDAO.findAll(example, pageable);

            Page<CarResponse> carResponsesPage = carsPage.map(this::formCarResponse);

            return ResponseEntity.status(HttpStatus.ACCEPTED).body(carResponsesPage);
        } catch (Exception e) {
            throw new CustomException("Failed fetch: " + e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    public ResponseEntity<CarResponse> getById(int id, HttpServletRequest request) {
        try {
            if (carDAO.findById(id).isEmpty()) {
                throw new CustomException("Car doesnt exist", HttpStatus.BAD_REQUEST);
            }
            CarSQL carSQL = carDAO.findById(id).get();

            ManagerSQL managerSQL = commonService.extractManagerFromHeader(request);
            AdministratorSQL administratorSQL = commonService.extractAdminFromHeader(request);

            if (!carSQL.isActivated()) {
                if (managerSQL == null
                        && administratorSQL == null) {
                    throw new CustomException("The announcement is not activated", HttpStatus.FORBIDDEN);
                }
            }

            CarResponse carResponse = formCarResponse(carSQL);

            return new ResponseEntity<>(carResponse, HttpStatus.ACCEPTED);
        } catch (CustomException e) {
            throw new CustomException("Failed fetch: " + e.getMessage(), e.getStatus());
        } catch (Exception e) {
            throw new CustomException("Failed fetch: " + e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    public CarSQL extractById(int id) {
        try {
            if (carDAO.findById(id).isEmpty()) {
                throw new CustomException("Car doesnt exist", HttpStatus.BAD_REQUEST);
            }
            return carDAO.findById(id).get();
        } catch (CustomException e) {
            throw new CustomException("Failed fetch: " + e.getMessage(), e.getStatus());
        } catch (Exception e) {
            throw new CustomException("Failed fetch: " + e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    public void checkCredentials(HttpServletRequest request, int id) {
        try {
            SellerSQL sellerFromHeader = commonService.extractSellerFromHeader(request);
            CarSQL carSQL = extractById(id);
            if (sellerFromHeader != null && sellerFromHeader.getId() != carSQL.getSeller().getId()) {
                throw new CustomException("Access_denied: check credentials", HttpStatus.FORBIDDEN);
            }
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        } catch (Exception e) {
            throw new CustomException("Failed fetch: " + e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    public void isPremium(HttpServletRequest request) {
        try {
            SellerSQL sellerSQL = commonService.extractSellerFromHeader(request);

            if (sellerSQL != null && sellerSQL.getAccountType().equals(EAccountType.BASIC)) {
                throw new CustomException("Premium plan required", HttpStatus.PAYMENT_REQUIRED);
            }
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        } catch (Exception e) {
            throw new CustomException("Failed fetch: " + e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    public ResponseEntity<String> activate(int id) {
        try {
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
        } catch (Exception e) {
            throw new CustomException("Failed activate: " + e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    public ResponseEntity<String> ban(int id) {
        try {
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
        } catch (Exception e) {
            throw new CustomException("Failed ban: " + e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    public ResponseEntity<Page<CarResponse>> getBySeller(SellerSQL seller, int page) {
        try {
            Pageable pageable = PageRequest.of(page, 2);

            Page<CarSQL> carsPage = carDAO.findAllBySeller(seller, pageable);

            Page<CarResponse> carResponsesPage = carsPage.map(this::formCarResponse);

            return new ResponseEntity<>(carResponsesPage, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            throw new CustomException("Failed fetch: " + e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    public List<CarSQL> findAllBySeller(SellerSQL seller) {
        try {
            return carDAO.findBySeller(seller);
        } catch (Exception e) {
            throw new CustomException("Failed fetch: " + e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    public ResponseEntity<CarResponse> post(@Valid CarDTO carDTO, SellerSQL seller, AdministratorSQL administratorSQL) {
        try {
            //todo seller response
            CarSQL car = CarSQL.builder()
                    .brand(carDTO.getBrand())
                    .powerH(carDTO.getPowerH())
                    .city(carDTO.getCity())
                    .region(carDTO.getRegion())
                    .model(carDTO.getModel())
                    .price(carDTO.getPrice())
                    .currency(carDTO.getCurrency())
                    .description(carDTO.getDescription())
                    .photo(carDTO.getPhoto())
                    .isActivated(carDTO.isActivated())
                    .build();

            if (administratorSQL != null) {
                car.setSeller(SellerSQL.adminBuilder()
                        .id(administratorSQL.getId())
                        .roles(List.of(ERole.ADMIN, ERole.ADMIN_GLOBAL))
                        .name(environment.getProperty("office.name"))
                        .region(environment.getProperty("office.region"))
                        .city(environment.getProperty("office.city"))
                        .build());
            } else {
                car.setSeller(seller);
            }

            CarSQL carSQL = carDAO.save(car);

            return new ResponseEntity<>(formCarResponse(carSQL), HttpStatus.ACCEPTED);
        } catch (Exception e) {
            throw new CustomException("Failed post: " + e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    public ResponseEntity<String> deleteById(int id) {
        try {
            CarSQL car = extractById(id);

            carDAO.deleteById(id);
            try {
                HashMap<String, Object> vars = new HashMap<>();
                vars.put("name", car.getSeller().getName());
                vars.put("description", car.getDescription());

                mailer.sendEmail(car.getSeller().getEmail(), EMail.CAR_BEING_BANNED, vars);

            } catch (Exception ignore) {
            }
            return new ResponseEntity<>("Success.Car_deleted", HttpStatus.GONE);
        } catch (Exception e) {
            throw new CustomException("Failed delete: " + e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    public ResponseEntity<CarResponse> update(int id, CarUpdateDTO carDTO) {
        try {
            CarSQL car = extractById(id);
            CarResponse carResponse;

            assert car != null;
            Class<?> carDTOClass = carDTO.getClass();
            Field[] fields = carDTOClass.getDeclaredFields();

            for (Field field : fields) {

                field.setAccessible(true);

                String fieldName = field.getName();
                Object fieldValue = field.get(carDTO);

                if (fieldValue != null) {
                    Field carField = CarSQL.class.getDeclaredField(fieldName);
                    carField.setAccessible(true);
                    if (fieldName.equals("currency")) {

                        carField.set(car, ECurrency.valueOf(fieldValue.toString()));
                    }

                    carField.set(car, fieldValue);
                }
            }

            CarSQL carSQL = carDAO.save(car);

            carResponse = formCarResponse(carSQL);

            return ResponseEntity.ok(carResponse);
        } catch (Exception e) {
            throw new CustomException("Error.Update_fail: " + e.getMessage(), HttpStatus.FORBIDDEN);
        }
    }

    private CarResponse formCarResponse(CarSQL carSQL) {
        try {

            CurrencyConverterResponse converterResponse = currencyConverterService.convert(carSQL.getCurrency(), carSQL.getPrice());

            return CarResponse.builder()
                    .id(carSQL.getId())
                    .brand(carSQL.getBrand())
                    .powerH(carSQL.getPowerH())
                    .city(carSQL.getCity())
                    .region(carSQL.getRegion())
                    .model(carSQL.getModel())
                    .price(carSQL.getPrice())
                    .currency(carSQL.getCurrency())
                    .photo(carSQL.getPhoto())
                    .seller(SellerCarResponse.builder()
                            .id(carSQL.getSeller().getId())
                            .name(carSQL.getSeller().getName())
                            .lastName(carSQL.getSeller().getLastName())
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
        } catch (CustomException e) {
            throw new CustomException("Failed to form response: " + e.getMessage(), e.getStatus());
        } catch (Exception e) {
            throw new CustomException("Failed to form response: " + e.getMessage(), HttpStatus.CONFLICT);
        }
    }


}