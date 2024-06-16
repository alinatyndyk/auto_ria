package com.example.auto_ria.controllers;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.auto_ria.currency_converter.ExchangeRateCache;
import com.example.auto_ria.dto.CarDTO;
import com.example.auto_ria.dto.requests.CarDTORequest;
import com.example.auto_ria.dto.updateDTO.CarUpdateDTO;
import com.example.auto_ria.enums.EBrand;
import com.example.auto_ria.enums.EMail;
import com.example.auto_ria.enums.EModel;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.mail.FMService;
import com.example.auto_ria.models.CarSQL;
import com.example.auto_ria.models.responses.car.CarResponse;
import com.example.auto_ria.models.responses.car.MiddlePriceResponse;
import com.example.auto_ria.models.responses.currency.ExchangeRateResponse;
import com.example.auto_ria.models.responses.statistics.StatisticsResponse;
import com.example.auto_ria.models.user.AdministratorSQL;
import com.example.auto_ria.models.user.ManagerSQL;
import com.example.auto_ria.models.user.UserSQL;
import com.example.auto_ria.services.CommonService;
import com.example.auto_ria.services.car.CarsServiceMySQLImpl;
import com.example.auto_ria.services.otherApi.CitiesService;
import com.example.auto_ria.services.otherApi.MixpanelService;
import com.example.auto_ria.services.otherApi.ProfanityFilterService;
import com.example.auto_ria.services.user.ManagerServiceMySQL;
import com.example.auto_ria.services.user.UsersServiceMySQLImpl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@AllArgsConstructor
@RequestMapping(value = "cars")

public class CarController {

    private CarsServiceMySQLImpl carsService;
    private UsersServiceMySQLImpl usersServiceMySQL;
    private CommonService commonService;
    private MixpanelService mixpanelService;
    private ProfanityFilterService profanityFilterService;
    private FMService mailer;
    private ManagerServiceMySQL managerServiceMySQL;
    private CitiesService citiesService;

    private static final AtomicInteger validationFailureCounter = new AtomicInteger(0);

    @GetMapping("page/{page}")
    public ResponseEntity<Page<CarResponse>> getAllPageQuery(
            @PathVariable("page") int page,
            @RequestParam Map<String, String> queryParams) {
        try {
            CarSQL carQueryParams = new CarSQL();

            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                String fieldName = entry.getKey();
                String fieldValue = entry.getValue();

                try {
                    Field field = CarSQL.class.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    if (fieldValue != null) {
                        switch (fieldName) {
                            case "brand" -> field.set(carQueryParams, EBrand.valueOf(fieldValue));
                            case "model" -> field.set(carQueryParams, EModel.valueOf(fieldValue));
                            case "powerH" -> field.set(carQueryParams, Integer.valueOf(fieldValue));
                            default -> field.set(carQueryParams, fieldValue);
                        }
                    }
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new CustomException("Forbidden query params found", HttpStatus.FORBIDDEN);
                }
            }

            carQueryParams.setActivated(true);
            return carsService.getAll(page, carQueryParams);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PostMapping("/viewed/{id}")
    public void addView(
            @PathVariable("id") int id) {
        try {
            CarSQL carSQL = carsService.extractById(id);
            if (!carSQL.isActivated()) {
                throw new CustomException("The car is temporally banned", HttpStatus.FORBIDDEN);
            }
            mixpanelService.view(String.valueOf(id));
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PostMapping("/activate/{id}")
    public ResponseEntity<String> activate(
            @PathVariable("id") int id) {
        try {

            CarSQL carSQL = carsService.extractById(id);

            if (carSQL.isActivated()) {
                throw new CustomException("The car is already active", HttpStatus.FORBIDDEN);
            }

            return carsService.activate(carSQL);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PostMapping("/ban/{id}")
    public ResponseEntity<String> banCar(
            @PathVariable("id") int id) {
        try {

            CarSQL carSQL = carsService.extractById(id);

            if (!carSQL.isActivated()) {
                throw new CustomException("The car is already banned", HttpStatus.FORBIDDEN);
            }

            return carsService.ban(carSQL);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @GetMapping("/middle/{id}")
    public ResponseEntity<MiddlePriceResponse> middle(
            @PathVariable("id") int id,
            HttpServletRequest request) {
        try {
            carsService.isPremium(request);
            CarSQL carSQL = carsService.extractById(id);

            if (!carSQL.isActivated()) {
                throw new CustomException("The car is banned", HttpStatus.FORBIDDEN);
            }

            carsService.checkCredentials(request, id);
            return carsService.getMiddlePrice(carSQL.getModel(), carSQL.getRegion());
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @GetMapping("/by-user/{id}/page/{page}")
    public ResponseEntity<Page<CarResponse>> getAllBySeller(
            @PathVariable("page") int page,
            @PathVariable("id") int id,
            HttpServletRequest request) {
        try {
            UserSQL userSQL = usersServiceMySQL.getById(id);

            if (commonService.extractManagerFromHeader(request) != null ||
                    commonService.extractAdminFromHeader(request) != null ||
                    commonService.extractUserFromHeader(request).getId() == id) {
                return carsService.getByUser(userSQL, page);
            } else {
                return carsService.getByUserActivatedOnly(userSQL, page);
            }

        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @GetMapping("/statistics/{id}")
    public ResponseEntity<StatisticsResponse> getStatistics(
            @PathVariable("id") int id,
            HttpServletRequest request) {
        try {
            carsService.isPremium(request);
            CarSQL carSQL = carsService.extractById(id);

            if (carSQL.isActivated()) {
                throw new CustomException("The car is already banned", HttpStatus.FORBIDDEN);
            }

            return ResponseEntity.ok(mixpanelService.getCarViewsStatistics(String.valueOf(id)));
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }

    }

    @GetMapping("/{id}")
    public ResponseEntity<CarResponse> getById(
            HttpServletRequest request,
            @PathVariable("id") int id) {
        try {
            return carsService.getById(id, request);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @GetMapping("/currency-rates")
    public ResponseEntity<ExchangeRateResponse> getCurrencyRates() {
        try {
            return ResponseEntity.ok(ExchangeRateResponse.builder()
                    .eurBuy(ExchangeRateCache.getEurBuy())
                    .eurSell(ExchangeRateCache.getEurSell())
                    .usdBuy(ExchangeRateCache.getUsdBuy())
                    .usdSell(ExchangeRateCache.getUsdSell())
                    .build());
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PostMapping()
    public ResponseEntity<CarResponse> post(
            @ModelAttribute @Valid CarDTORequest carDTO,
            HttpServletRequest request) {
        try {
            UserSQL user = commonService.extractUserFromHeader(request);
            AdministratorSQL administratorSQL = commonService.extractAdminFromHeader(request);

            citiesService.isValidUkrainianCity(carDTO.getRegion(), carDTO.getCity());

            CarDTO car = CarDTO
                    .builder()
                    .brand(carDTO.getBrand())
                    .powerH(carDTO.getPowerH())
                    .city(carDTO.getCity())
                    .region(carDTO.getRegion())
                    .model(carDTO.getModel())
                    .price(carDTO.getPrice())
                    .isActivated(true)
                    .currency(carDTO.getCurrency())
                    .isActivated(true)
                    .description(carDTO.getDescription())
                    .build();

            System.out.println("is premium checked");
            if (administratorSQL == null ) {
                //&& !carsService.findAllByUser(user).isEmpty()
                System.out.println("is premium checked1");
                // carsService.isPremium(request);
                System.out.println("is premium checked2");
            }

            String filteredText = profanityFilterService.containsProfanity(carDTO.getDescription());

            if (profanityFilterService.containsProfanityBoolean(filteredText, carDTO.getDescription())) {
                int currentCount = validationFailureCounter.incrementAndGet();
                if (currentCount > 3) {
                    car.setActivated(false);
                    try {
                        HashMap<String, Object> vars = new HashMap<>();
                        List<ManagerSQL> managers = managerServiceMySQL.getAll();

                        String email;

                        if (user != null) {
                            vars.put("name", user.getName());
                            email = user.getEmail();
                        } else if (administratorSQL != null) {
                            vars.put("name", administratorSQL.getName());
                            email = administratorSQL.getEmail();
                        } else {
                            throw new CustomException("Invalid token user", HttpStatus.FORBIDDEN);
                        }
                        vars.put("description", car.getDescription());

                        managers.forEach(managerSQLItem -> {
                            try {
                                mailer.sendEmail(managerSQLItem.getEmail(), EMail.CHECK_ANNOUNCEMENT, vars);
                            } catch (Exception ignore) {
                            }
                        });
                        mailer.sendEmail(email, EMail.CAR_BEING_CHECKED, vars);
                    } catch (Exception e) {
                        throw new CustomException("Error sending emails", HttpStatus.EXPECTATION_FAILED);
                    }
                } else {
                    int attemptsLeft = 4 - currentCount;
                    throw new CustomException("Consider editing your description. " +
                            "Profanity found - attempts left:  " + attemptsLeft, HttpStatus.BAD_REQUEST);
                }
            }

            if (carDTO.getPictures() != null) {
                commonService.transferPhotos(carDTO.getPictures());

                List<String> names = new ArrayList<>();

                for (MultipartFile file : carDTO.getPictures()) {
                    names.add(file.getOriginalFilename());
                }

                car.setPhoto(names);
            }

            return carsService.post(car, user, administratorSQL);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CarResponse> patchCar(@PathVariable int id,
            @RequestBody @Valid CarUpdateDTO partialCar,
            HttpServletRequest request) {
        try {
            CarSQL carSQL = carsService.extractById(id);

            UserSQL userSQL = commonService.extractUserFromHeader(request);
            AdministratorSQL administratorSQL = commonService.extractAdminFromHeader(request);

            carsService.checkCredentials(request, id);

            citiesService.isValidUkrainianCity(partialCar.getRegion(), partialCar.getCity());

            String filteredText = profanityFilterService.containsProfanity(partialCar.getDescription());

            if (profanityFilterService.containsProfanityBoolean(filteredText, partialCar.getDescription())) {
                int currentCount = validationFailureCounter.incrementAndGet();
                if (currentCount > 3) {

                    carSQL.setActivated(false);
                    carsService.save(carSQL);
                    try {
                        HashMap<String, Object> vars = new HashMap<>();
                        List<ManagerSQL> managers = managerServiceMySQL.getAll();

                        String email;

                        if (userSQL != null) {
                            vars.put("name", userSQL.getName());
                            email = userSQL.getEmail();
                        } else if (administratorSQL != null) {
                            vars.put("name", administratorSQL.getName());
                            email = administratorSQL.getEmail();
                        } else {
                            throw new CustomException("Invalid token user", HttpStatus.FORBIDDEN);
                        }
                        vars.put("description", partialCar.getDescription());

                        managers.forEach(managerSQLItem -> {
                            try {
                                mailer.sendEmail(managerSQLItem.getEmail(), EMail.CHECK_ANNOUNCEMENT, vars);
                            } catch (Exception ignore) {
                            }
                        });
                        mailer.sendEmail(email, EMail.CAR_BEING_CHECKED, vars);
                    } catch (Exception e) {
                        throw new CustomException("Error sending emails", HttpStatus.EXPECTATION_FAILED);
                    }
                } else {
                    int attemptsLeft = 4 - currentCount;
                    throw new CustomException("Consider editing your description. " +
                            "Profanity found - attempts left:  " + attemptsLeft, HttpStatus.BAD_REQUEST);
                }
            }

            return carsService.update(id, partialCar);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PatchMapping("photos/{id}")
    public ResponseEntity<String> patchPhotos(@PathVariable int id,
            @RequestParam("pictures[]") MultipartFile[] newPictures,
            HttpServletRequest request) {
        try {
            carsService.checkCredentials(request, id);

            CarSQL carSQL = carsService.extractById(id);

            List<String> newPicNames = new ArrayList<>();

            for (MultipartFile file : newPictures) {
                newPicNames.add(file.getOriginalFilename());
            }
            List<String> alreadyOnServer = new ArrayList<>();

            for (String photoName : carSQL.getPhoto()) {
                if (!newPicNames.contains(photoName)) {
                    commonService.removeAvatar(photoName);
                } else {
                    alreadyOnServer.add(photoName);
                }
            }

            for (MultipartFile pic : newPictures) {
                if (!alreadyOnServer.contains(pic.getOriginalFilename())) {
                    commonService.transferAvatar(pic, pic.getOriginalFilename());
                }
            }

            carSQL.setPhoto(newPicNames);

            return ResponseEntity.status(HttpStatus.ACCEPTED).body("Files successfully uploaded");
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteById(@PathVariable int id, HttpServletRequest request) {
        try {
            List<String> pictures = carsService.extractById(id).getPhoto();

            carsService.checkCredentials(request, id);

            pictures.forEach(picture -> commonService.removeAvatar(picture));

            return carsService.deleteById(id);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @GetMapping("/brands")
    public ResponseEntity<EBrand[]> getBrands() {
        try {
            return ResponseEntity.ok().body(Arrays.stream(EBrand.values()).toArray(EBrand[]::new));
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @GetMapping("/brands/{brand}/models")
    public ResponseEntity<EModel[]> getBrandsModels(@PathVariable("brand") String brand) {
        try {
            return ResponseEntity.ok().body(Arrays.stream(EModel.values())
                    .filter(eModel -> eModel.getBrand().name().matches(brand))
                    .toArray(EModel[]::new));
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

}
