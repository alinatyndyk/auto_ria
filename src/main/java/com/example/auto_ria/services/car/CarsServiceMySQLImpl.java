package com.example.auto_ria.services.car;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.example.auto_ria.dao.CarDaoSQL;
import com.example.auto_ria.dto.CarDTO;
import com.example.auto_ria.dto.updateDTO.CarUpdateDTO;
import com.example.auto_ria.enums.EAccountType;
import com.example.auto_ria.enums.ECurrency;
import com.example.auto_ria.enums.EMail;
import com.example.auto_ria.enums.EModel;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.mail.FMService;
import com.example.auto_ria.models.CarSQL;
import com.example.auto_ria.models.responses.car.CarResponse;
import com.example.auto_ria.models.responses.car.MiddlePriceResponse;
import com.example.auto_ria.models.responses.currency.CurrencyConverterResponse;
import com.example.auto_ria.models.responses.user.UserCarResponse;
import com.example.auto_ria.models.user.UserSQL;
import com.example.auto_ria.services.CommonService;
import com.example.auto_ria.services.currency.CurrencyConverterService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CarsServiceMySQLImpl {

    private CarDaoSQL carDAO;
    private CommonService commonService;
    private FMService mailer;
    private CurrencyConverterService currencyConverterService;

    public ResponseEntity<List<CarSQL>> getAll() {
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            return new ResponseEntity<>(carDAO.findAll(), httpHeaders, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            throw new CustomException("Failed fetch: " + e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    public ResponseEntity<MiddlePriceResponse> getMiddlePrice(EModel model, String region) {
        try {

            double totalInUSD = 0.00;
            double totalInUAH = 0.00;
            double totalInEUR = 0.00;

            double middleInUSD = 0.00;
            double middleInUAH = 0.00;
            double middleInEUR = 0.00;

            HashMap<String, Object> params = new HashMap<>();
            params.put("model", model);
            params.put("region", region);

            List<Map<String, Object>> pricesAndCurrencies = carDAO.findPricesByBrandAndRegion(params);

            for (Map<String, Object> map : pricesAndCurrencies) {
                String currency = map.get("currency").toString();
                String price = map.get("price").toString();

                CurrencyConverterResponse response = currencyConverterService.convert(ECurrency.valueOf(currency),
                        price);

                totalInUAH = totalInUAH + response.getCurrencyHashMap().get(ECurrency.UAH);
                totalInEUR = totalInEUR + response.getCurrencyHashMap().get(ECurrency.EUR);
                totalInUSD = totalInUSD + response.getCurrencyHashMap().get(ECurrency.USD);

                middleInUAH = totalInUAH / carDAO.countByModelAndRegion(model, region);
                middleInUSD = totalInUSD / carDAO.countByModelAndRegion(model, region);
                middleInEUR = totalInEUR / carDAO.countByModelAndRegion(model, region);

            }

            return ResponseEntity.ok(MiddlePriceResponse.builder()
                    .middleInUAH(middleInUAH)
                    .middleInEUR(middleInEUR)
                    .middleInUSD(middleInUSD)
                    .build());
        } catch (Exception e) {
            throw new CustomException("Could not get middle price for current item: " + e.getMessage(),
                    HttpStatus.EXPECTATION_FAILED);
        }
    }

    public ResponseEntity<Page<CarResponse>> getAll(int page, CarSQL params) {
        try {
            ExampleMatcher matcher = ExampleMatcher.matchingAll()
                    .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                    .withIgnoreNullValues()
                    .withIgnorePaths("powerH")
                    .withIgnorePaths("id")
                    .withIgnorePaths("photo");

            Example<CarSQL> example = Example.of(params, matcher);

            Pageable pageable = PageRequest.of(page, 2);
            Page<CarSQL> carsPage = carDAO.findAll(example, pageable);

            Page<CarResponse> carResponsesPage = carsPage.map(this::formCarResponse);

            return ResponseEntity.status(HttpStatus.ACCEPTED).body(carResponsesPage);
        } catch (Exception e) {
            throw new CustomException("Failed fetch" + e.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }
    }

    public ResponseEntity<CarResponse> getById(int id, HttpServletRequest request) {
        try {
            if (carDAO.findById(id).isEmpty()) {
                throw new CustomException("Car doesnt exist", HttpStatus.BAD_REQUEST);
            }
            CarSQL carSQL = carDAO.findById(id).get();

            if (!carSQL.isActivated()) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication.getPrincipal() instanceof UserDetails) {
                    UserDetails userDetails = (UserDetails) authentication.getPrincipal();

                    if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("USER")) &&
                            carSQL.getUser().getEmail() != userDetails.getUsername()) {
                        throw new CustomException("The car is banned", HttpStatus.FORBIDDEN);
                    }
                } else {
                    throw new CustomException("The car is banned", HttpStatus.UNAUTHORIZED);
                }
            }

            CarResponse carResponse = formCarResponse(carSQL);

            return new ResponseEntity<>(carResponse, HttpStatus.ACCEPTED);
        } catch (CustomException e) {
            throw new CustomException("Failed fetch: " + e.getMessage(), e.getStatus());
        } catch (Exception e) {
            throw new CustomException("Failed fetch", HttpStatus.EXPECTATION_FAILED);
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
            throw new CustomException("Failed fetch", HttpStatus.EXPECTATION_FAILED);
        }
    }

    public void checkCredentials(HttpServletRequest request, int id) {
        try {
            UserSQL userFromHeader = commonService.extractUserFromHeader(request);
            CarSQL carSQL = extractById(id);
            if (userFromHeader != null && userFromHeader.getId() != carSQL.getUser().getId()) {
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
            UserSQL userSQL = commonService.extractUserFromHeader(request);

            if (userSQL != null && userSQL.getAccountType().equals(EAccountType.BASIC)) {
                throw new CustomException("Premium plan required", HttpStatus.PAYMENT_REQUIRED);
            }
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        } catch (Exception e) {
            throw new CustomException("Failed fetch: " + e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    public ResponseEntity<String> activate(CarSQL carSQL) {
        try {
            carSQL.setActivated(true);
            carDAO.save(carSQL);
            try {
                HashMap<String, Object> vars = new HashMap<>();
                vars.put("name", carSQL.getUser().getName());
                vars.put("car_id", carSQL.getId());
                mailer.sendEmail(carSQL.getUser().getEmail(), EMail.CAR_BEING_ACTIVATED, vars);
            } catch (Exception ignore) {
            }
            return ResponseEntity.ok("Car activated successfully");
        } catch (Exception e) {
            throw new CustomException("Failed activate: " + e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    public ResponseEntity<String> ban(CarSQL carSQL) {
        try {
            carSQL.setActivated(false);
            carDAO.save(carSQL);
            try {
                HashMap<String, Object> vars = new HashMap<>();
                vars.put("name", carSQL.getUser().getName());
                vars.put("car_id", carSQL.getId());
                mailer.sendEmail(carSQL.getUser().getEmail(), EMail.CAR_BEING_BANNED, vars);
            } catch (Exception ignore) {
            }
            return ResponseEntity.ok("Car banned successfully");
        } catch (Exception e) {
            throw new CustomException("Failed ban: " + e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    public ResponseEntity<Page<CarResponse>> getByUser(UserSQL user, int page) {
        try {

            Pageable pageable = PageRequest.of(page, 2);
            Page<CarSQL> carsPage = carDAO.findAllByUser(user, pageable);

            Page<CarResponse> carResponsesPage = carsPage.map(this::formCarResponse);

            return new ResponseEntity<>(carResponsesPage, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            throw new CustomException("Failed fetch", HttpStatus.EXPECTATION_FAILED);
        }
    }

    public ResponseEntity<Page<CarResponse>> getByUserActivatedOnly(UserSQL user, int page) {
        try {

            Pageable pageable = PageRequest.of(page, 2);
            Page<CarSQL> carsPage = carDAO.findAllByUserAndActivatedTrue(user, pageable);
            System.out.println(carsPage + "car page--------------------");

            Page<CarResponse> carResponsesPage = carsPage.map(this::formCarResponse);

            return new ResponseEntity<>(carResponsesPage, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            throw new CustomException("Failed fetch" + e.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }
    }

    public void save(CarSQL car) {
        try {
            carDAO.save(car);
        } catch (Exception e) {
            throw new CustomException("Failed save" + e.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }
    }

    public List<CarSQL> findAllByUser(UserSQL user) {
        try {
            return carDAO.findByUser(user);
        } catch (Exception e) {
            throw new CustomException("Failed fetch: " + e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    public ResponseEntity<CarResponse> post(@Valid CarDTO carDTO, UserSQL user) {
        try {
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

            car.setUser(user);

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
                vars.put("name", car.getUser().getName());
                vars.put("description", car.getDescription());

                mailer.sendEmail(car.getUser().getEmail(), EMail.CAR_BEING_BANNED, vars);

            } catch (Exception ignore) {
            }
            return ResponseEntity.ok("Success.Car_deleted");
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

            CurrencyConverterResponse converterResponse = currencyConverterService.convert(carSQL.getCurrency(),
                    carSQL.getPrice());

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
                    .isActivated(carSQL.isActivated())
                    .user(UserCarResponse.builder()
                            .id(carSQL.getUser().getId())
                            .name(carSQL.getUser().getName())
                            .lastName(carSQL.getUser().getLastName())
                            .avatar(carSQL.getUser().getAvatar())
                            .city(carSQL.getUser().getCity())
                            .region(carSQL.getUser().getRegion())
                            .role(carSQL.getUser().getRoles().get(0))
                            .number(carSQL.getUser().getNumber())
                            .createdAt(carSQL.getUser().getCreatedAt())
                            .build())
                    .description(carSQL.getDescription())
                    .priceUAH(converterResponse.getCurrencyHashMap().get(ECurrency.UAH))
                    .priceUSD(converterResponse.getCurrencyHashMap().get(ECurrency.USD))
                    .priceEUR(converterResponse.getCurrencyHashMap().get(ECurrency.EUR))
                    .createdAt(carSQL.getCreatedAt())
                    .build();
        } catch (CustomException e) {
            throw new CustomException("Failed to form response: " + e.getMessage(), e.getStatus());
        } catch (Exception e) {
            throw new CustomException("Failed to form response: " + e.getMessage(), HttpStatus.CONFLICT);
        }
    }

}
