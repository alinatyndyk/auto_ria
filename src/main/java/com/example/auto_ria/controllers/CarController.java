package com.example.auto_ria.controllers;

import com.example.auto_ria.currency_converter.ExchangeRateCache;
import com.example.auto_ria.dao.premium.PremiumPlanDaoSQL;
import com.example.auto_ria.dao.user.UserDaoSQL;
import com.example.auto_ria.dto.CarDTO;
import com.example.auto_ria.dto.requests.CarDTORequest;
import com.example.auto_ria.dto.updateDTO.CarUpdateDTO;
import com.example.auto_ria.enums.EBrand;
import com.example.auto_ria.enums.EMail;
import com.example.auto_ria.enums.EModel;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.mail.FMService;
import com.example.auto_ria.models.user.AdministratorSQL;
import com.example.auto_ria.models.user.CarSQL;
import com.example.auto_ria.models.user.ManagerSQL;
import com.example.auto_ria.models.user.SellerSQL;
import com.example.auto_ria.models.responses.CarResponse;
import com.example.auto_ria.models.responses.ExchangeRateResponse;
import com.example.auto_ria.models.responses.MiddlePriceResponse;
import com.example.auto_ria.models.responses.StatisticsResponse;
import com.example.auto_ria.services.*;
import com.example.auto_ria.services.car.CarsServiceMySQLImpl;
import com.example.auto_ria.services.otherApi.CitiesService;
import com.example.auto_ria.services.otherApi.MixpanelService;
import com.example.auto_ria.services.otherApi.ProfanityFilterService;
import com.example.auto_ria.services.otherApi.StripeService;
import com.example.auto_ria.services.user.ManagerServiceMySQL;
import com.example.auto_ria.services.user.UsersServiceMySQLImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@AllArgsConstructor
@RequestMapping(value = "cars")
//todo everything in try catch
//todo cancel sub premium
//todo ban/activate users
//todo chat

// ! todo premium bought monthly
// ! todo validation
// ! todo check ban/activate car
// ! todo check isActivated without permissions
// ! todo show only not banned cars
public class CarController {

    private CarsServiceMySQLImpl carsService;
    private UsersServiceMySQLImpl usersServiceMySQL;
    private CommonService commonService;
    private UserDaoSQL userDaoSQL;
    private MixpanelService mixpanelService;
    private ProfanityFilterService profanityFilterService;
    private StripeService stripeService;
    private FMService mailer;
    private ManagerServiceMySQL managerServiceMySQL;
    private CitiesService citiesService;

    private PremiumPlanDaoSQL premiumPlanDaoSQL;

    private Environment environment;

    private static final AtomicInteger validationFailureCounter = new AtomicInteger(0);

    @GetMapping("page/{page}")
    public ResponseEntity<Page<CarResponse>> getAllPageQuery(
            @PathVariable("page") int page,
            @RequestParam Map<String, String> queryParams
    ) {
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
            @PathVariable("id") int id
    ) {
        try {
            carsService.extractById(id);
            mixpanelService.view(String.valueOf(id));
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PostMapping("/activate/{id}")
    public ResponseEntity<String> activate(
            @PathVariable("id") int id
    ) {
        try {
            return carsService.activate(id);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PostMapping("/ban/{id}")
    public ResponseEntity<String> banCar(
            @PathVariable("id") int id
    ) {
        try {
            return carsService.ban(id);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @GetMapping("/middle/{id}")
    public ResponseEntity<MiddlePriceResponse> middle(
            @PathVariable("id") int id,
            HttpServletRequest request
    ) {
        try {
            carsService.isPremium(request);
            CarSQL carSQL = carsService.extractById(id);
            carsService.checkCredentials(request, id);
            return carsService.getMiddlePrice(carSQL.getBrand(), carSQL.getRegion());
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @GetMapping("/by-seller/page/{page}")
    public ResponseEntity<Page<CarResponse>> getAllBySeller(
            @PathVariable("page") int page,
            @RequestParam("id") int id
    ) {
        try {
            SellerSQL sellerSQL = usersServiceMySQL.getById(id);
            return carsService.getBySeller(sellerSQL, page);
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
            carsService.extractById(id);
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
            @RequestPart("pictures[]") MultipartFile[] pictures,
            HttpServletRequest request
    ) {
        try {
            SellerSQL seller = commonService.extractSellerFromHeader(request);
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


            if (!carsService.findAllBySeller(seller).isEmpty()) {
                carsService.isPremium(request);
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

                        if (seller != null) {
                            vars.put("name", seller.getName());
                            email = seller.getEmail();
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

            commonService.transferPhotos(pictures);

            List<String> names = new ArrayList<>();

            for (MultipartFile file : pictures) {
                names.add(file.getOriginalFilename());
            }

            car.setPhoto(names);

            return carsService.post(car, seller, administratorSQL);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CarResponse> patchCar(@PathVariable int id,
                                                @RequestBody @Valid CarUpdateDTO partialCar,
                                                HttpServletRequest request) {
        try {
            carsService.checkCredentials(request, id);
            citiesService.isValidUkrainianCity(partialCar.getRegion(), partialCar.getCity());
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

            Arrays.stream(newPictures)
                    .filter(pic -> !alreadyOnServer.contains(pic.getOriginalFilename()))
                    .forEach(pic -> {
                        commonService.transferAvatar(pic, pic.getOriginalFilename());
                    });

            carSQL.setPhoto(newPicNames);

            return ResponseEntity.status(HttpStatus.ACCEPTED).body("Files successfully uploaded");
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteById(@PathVariable int id, HttpServletRequest request) {
        try {
            carsService.checkCredentials(request, id);

            List<String> pictures = carsService.extractById(id).getPhoto();

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
