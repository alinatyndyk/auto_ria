package com.example.auto_ria.controllers;

import com.example.auto_ria.currency_converter.ExchangeRateCache;
import com.example.auto_ria.dao.UserDaoSQL;
import com.example.auto_ria.dto.CarDTO;
import com.example.auto_ria.dto.CarDTORequest;
import com.example.auto_ria.dto.updateDTO.CarUpdateDTO;
import com.example.auto_ria.enums.*;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.mail.FMService;
import com.example.auto_ria.models.AdministratorSQL;
import com.example.auto_ria.models.CarSQL;
import com.example.auto_ria.models.ManagerSQL;
import com.example.auto_ria.models.SellerSQL;
import com.example.auto_ria.models.responses.CarResponse;
import com.example.auto_ria.models.responses.ExchangeRateResponse;
import com.example.auto_ria.models.responses.MiddlePriceResponse;
import com.example.auto_ria.models.responses.StatisticsResponse;
import com.example.auto_ria.services.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@AllArgsConstructor
@RequestMapping(value = "cars")
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
    private static final AtomicInteger validationFailureCounter = new AtomicInteger(0);

    @GetMapping("page/{page}")
    public ResponseEntity<Page<CarResponse>> getAllPageQuery(
            @PathVariable("page") int page,
            @RequestParam Map<String, String> queryParams
    ) {
        CarSQL carQueryParams = new CarSQL();

        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            String fieldName = entry.getKey();
            String fieldValue = entry.getValue();

            try {
                Field field = CarSQL.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                if (fieldValue != null) {
                    switch (fieldName) {
                        case "region" -> field.set(carQueryParams, ERegion.valueOf(fieldValue));
                        case "brand" -> field.set(carQueryParams, EBrand.valueOf(fieldValue));
                        case "model" -> field.set(carQueryParams, EModel.valueOf(fieldValue));
                        default -> field.set(carQueryParams, fieldValue);
                    }
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new CustomException("Forbidden query params found", HttpStatus.FORBIDDEN);
            }
        }
        return carsService.getAll(page, carQueryParams);
    }


    @PostMapping("/viewed/{id}")
    public void addView(
            @PathVariable("id") int id
    ) throws IOException {
        carsService.extractById(id);
        mixpanelService.view(String.valueOf(id));
    }

    @PostMapping("/buy-premium")
    public ResponseEntity<String> getPremium(
            HttpServletRequest request
    ) {
        SellerSQL sellerSQL = commonService.extractSellerFromHeader(request);
        if (sellerSQL.getAccountType().equals(EAccountType.PREMIUM)) {
            throw new CustomException("Premium account is already bought", HttpStatus.BAD_REQUEST);
        }
        stripeService.createPayment();
        sellerSQL.setAccountType(EAccountType.PREMIUM);
        userDaoSQL.save(sellerSQL);
        return ResponseEntity.ok("Premium bought successfully");
    }

    @PostMapping("/activate/{id}")
    public ResponseEntity<String> activate(
            @PathVariable("id") int id
    ) {
        return carsService.activate(id);
    }

    @PostMapping("/ban/{id}")
    public ResponseEntity<String> banCar(
            @PathVariable("id") int id
    ) {
        return carsService.ban(id);
    }

    @GetMapping("/middle/{id}")
    public ResponseEntity<MiddlePriceResponse> middle(
            @PathVariable("id") int id,
            HttpServletRequest request
    ) {
        carsService.isPremium(request);
        CarSQL carSQL = carsService.extractById(id);
        carsService.checkCredentials(request, id);
        return carsService.getMiddlePrice(carSQL.getBrand(), carSQL.getRegion());
    }

    @GetMapping("/by-seller/page/{page}")
    public ResponseEntity<Page<CarResponse>> getAllBySeller(
            @PathVariable("page") int page,
            @RequestParam("id") int id
    ) {
        SellerSQL sellerSQL = usersServiceMySQL.getById(id);
        return carsService.getBySeller(sellerSQL, page);
    }

    @GetMapping("/statistics/{id}")
    public ResponseEntity<StatisticsResponse> getStatistics(
            @PathVariable("id") int id,
            HttpServletRequest request) {
        carsService.isPremium(request);
        carsService.extractById(id);
        return ResponseEntity.ok(mixpanelService.getCarViewsStatistics(String.valueOf(id)));

    }

    @GetMapping("/{id}")
    public ResponseEntity<CarResponse> getById(
            HttpServletRequest request,
            @PathVariable("id") int id) {
        return carsService.getById(id, request);
    }

    @GetMapping("/currency-rates")
    public ResponseEntity<ExchangeRateResponse> getCurrencyRates() {
        return ResponseEntity.ok(ExchangeRateResponse.builder()
                .eurBuy(ExchangeRateCache.getEurBuy())
                .eurSell(ExchangeRateCache.getEurSell())
                .usdBuy(ExchangeRateCache.getUsdBuy())
                .usdSell(ExchangeRateCache.getUsdSell())
                .build());
    }

    @PostMapping()
    public ResponseEntity<CarResponse> post(
            @ModelAttribute @Valid CarDTORequest carDTO,
            @RequestPart("pictures[]") MultipartFile[] pictures,
            HttpServletRequest request
    ) throws IOException {

        SellerSQL seller = commonService.extractSellerFromHeader(request);
        AdministratorSQL administratorSQL = commonService.extractAdminFromHeader(request);

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
                    } else {
                        vars.put("name", administratorSQL.getName());
                        email = administratorSQL.getEmail();
                    }
                    vars.put("description", car.getDescription());

                    managers.forEach(managerSQL -> {
                        try {
                            mailer.sendEmail(managerSQL.getEmail(), EMail.CHECK_ANNOUNCEMENT, vars);
                        } catch (Exception ignore) {
                        }
                    });
                    mailer.sendEmail(email, EMail.CAR_BEING_CHECKED, vars);
                } catch (Exception ignore) {
                }
            } else {
                int attemptsLeft = 4 - currentCount;
                throw new CustomException("Consider editing your description. Profanity found - attempts left:  " + attemptsLeft, HttpStatus.BAD_REQUEST);
            }
        }

        commonService.transferPhotos(pictures);

        List<String> names = new ArrayList<>();

        for (MultipartFile file : pictures) {
            names.add(file.getOriginalFilename());
        }

        car.setPhoto(names);

        return carsService.post(car, seller, administratorSQL);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CarResponse> patchCar(@PathVariable int id,
                                                @RequestBody CarUpdateDTO partialCar,
                                                HttpServletRequest request) throws NoSuchFieldException, IllegalAccessException {
        carsService.checkCredentials(request, id);
        return carsService.update(id, partialCar);
    }

    @PatchMapping("photos/{id}")
    public ResponseEntity<String> patchPhotos(@PathVariable int id,
                                              @RequestParam("pictures[]") MultipartFile[] newPictures,
                                              HttpServletRequest request) throws IOException {
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
                    try {
                        commonService.transferAvatar(pic, pic.getOriginalFilename());
                    } catch (IOException e) {
                        throw new CustomException("Something went wrong while transporting the files. " +
                                "Try again later", HttpStatus.CONFLICT);
                    }
                });

        carSQL.setPhoto(newPicNames);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Files successfully uploaded");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteById(@PathVariable int id, HttpServletRequest request) {
        carsService.checkCredentials(request, id);

        List<String> pictures = carsService.extractById(id).getPhoto();

        pictures.forEach(picture -> {
            try {
                commonService.removeAvatar(picture);
            } catch (IOException e) {
                throw new CustomException("Failed: Transfer_photos. Try again later", HttpStatus.EXPECTATION_FAILED);
            }
        });

        return carsService.deleteById(id);
    }

}
