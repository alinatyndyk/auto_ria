package com.example.auto_ria.controllers;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
import com.example.auto_ria.enums.EAccountType;
import com.example.auto_ria.enums.EBrand;
import com.example.auto_ria.enums.EMail;
import com.example.auto_ria.enums.EModel;
import com.example.auto_ria.enums.ERole;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.mail.FMService;
import com.example.auto_ria.models.CarSQL;
import com.example.auto_ria.models.responses.car.CarResponse;
import com.example.auto_ria.models.responses.car.MiddlePriceResponse;
import com.example.auto_ria.models.responses.currency.ExchangeRateResponse;
import com.example.auto_ria.models.responses.statistics.StatisticsResponse;
import com.example.auto_ria.models.user.UserSQL;
import com.example.auto_ria.services.CommonService;
import com.example.auto_ria.services.car.CarsServiceMySQLImpl;
import com.example.auto_ria.services.otherApi.CitiesService;
import com.example.auto_ria.services.otherApi.MixpanelService;
import com.example.auto_ria.services.otherApi.ProfanityFilterService;
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
    private CitiesService citiesService;

    private static final AtomicInteger validationFailureCounter = new AtomicInteger(0);

    @GetMapping("page/{page}")
    public ResponseEntity<Page<CarResponse>> getAllPageQuery(
            @PathVariable("page") int page,
            @RequestParam Map<String, String> queryParams) {
        try {
            CarSQL carQueryParams = new CarSQL();
            page = page - 1;

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

    @PreAuthorize("hasRole('ADMIN', 'MANAGER')")
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

    @PreAuthorize("hasRole('ADMIN', 'MANAGER')")
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

    @PreAuthorize("hasRole('ADMIN', 'MANAGER', USER)")
    @GetMapping("/middle/{id}")
    public ResponseEntity<MiddlePriceResponse> middle(
            @PathVariable("id") int id,
            HttpServletRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();

                if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("USER"))) {
                    carsService.isPremium(request);
                }
            } else {
                throw new CustomException("Unauthorized", HttpStatus.UNAUTHORIZED);
            }
            CarSQL carSQL = carsService.extractById(id);

            if (!carSQL.isActivated()) {
                throw new CustomException("The car is banned", HttpStatus.FORBIDDEN);
            }

            return carsService.getMiddlePrice(carSQL.getModel(), carSQL.getRegion());
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @GetMapping("/by-user/{id}/page/{page}")
    public ResponseEntity<Page<CarResponse>> getAllBySeller(
            @PathVariable("page") int page,
            @PathVariable("id") int id) {
        try {
            UserSQL userSQL = usersServiceMySQL.getById(id);
            return carsService.getByUserActivatedOnly(userSQL, page);

        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PreAuthorize("hasRole('ADMIN', 'MANAGER', 'USER')")
    @GetMapping("/statistics/{id}")
    public ResponseEntity<StatisticsResponse> getStatistics(
            @PathVariable("id") int id,
            HttpServletRequest request) {
        try {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();

                if (userDetails.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("USER"))) {
                    carsService.isPremium(request);
                }
            } else {
                throw new CustomException("Unauthorized", HttpStatus.UNAUTHORIZED);
            }

            CarSQL carSQL = carsService.extractById(id);

            if (!carSQL.isActivated()) {
                throw new CustomException("The car is banned", HttpStatus.FORBIDDEN);
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

    @PreAuthorize("hasRole('ADMIN', 'USER')")
    @PostMapping()
    public ResponseEntity<CarResponse> post(
            @ModelAttribute @Valid CarDTORequest carDTO,
            HttpServletRequest request) {
        try {
            UserSQL user;

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

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                user = usersServiceMySQL.getByEmail(userDetails.getUsername());
                if (userDetails.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("USER"))
                        && !carsService.findAllByUser(user).isEmpty()) {

                    if (user.getAccountType().equals(EAccountType.BASIC)) {
                        throw new CustomException("Premium plan required", HttpStatus.PAYMENT_REQUIRED);
                    }
                }
            } else {
                throw new CustomException("Unauthorized", HttpStatus.UNAUTHORIZED);
            }
            String filteredText = profanityFilterService.containsProfanity(carDTO.getDescription());

            if (profanityFilterService.containsProfanityBoolean(filteredText, carDTO.getDescription())) {
                int currentCount = validationFailureCounter.incrementAndGet();
                if (currentCount > 3) {
                    car.setActivated(false);
                    try {
                        HashMap<String, Object> vars = new HashMap<>();
                        List<UserSQL> managers = usersServiceMySQL.findAllByRole(ERole.MANAGER);

                        String email;

                        if (user != null) {
                            vars.put("name", user.getName());
                            email = user.getEmail();
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

            return carsService.post(car, user);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PreAuthorize("hasRole('ADMIN', 'USER')")
    @PatchMapping("/{id}")
    public ResponseEntity<CarResponse> patchCar(@PathVariable int id,
            @RequestBody @Valid CarUpdateDTO partialCar,
            HttpServletRequest request) {
        try {
            CarSQL carSQL = carsService.extractById(id);

            UserSQL userSQL;

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                userSQL = usersServiceMySQL.getByEmail(userDetails.getUsername());
                if (userDetails.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("USER"))
                        && !carsService.findAllByUser(userSQL).isEmpty()) {
                    carsService.isPremium(request);
                }
            } else {
                throw new CustomException("Unauthorized", HttpStatus.UNAUTHORIZED);
            }

            if (partialCar.getCity() != null || partialCar.getRegion() != null) {
                citiesService.isValidUkrainianCity(partialCar.getRegion(), partialCar.getCity());
            }

            if (partialCar.getDescription() != null) {

                String filteredText = profanityFilterService.containsProfanity(partialCar.getDescription());

                if (profanityFilterService.containsProfanityBoolean(filteredText, partialCar.getDescription())) {
                    int currentCount = validationFailureCounter.incrementAndGet();
                    if (currentCount > 3) {

                        carSQL.setActivated(false);
                        carsService.save(carSQL);
                        try {
                            HashMap<String, Object> vars = new HashMap<>();
                            List<UserSQL> managers = usersServiceMySQL.findAllByRole(ERole.MANAGER);

                            String email;

                            if (userSQL != null) {
                                vars.put("name", userSQL.getName());
                                email = userSQL.getEmail();
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
            }

            return carsService.update(id, partialCar);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PostMapping("delete-pictures/{id}")
    public ResponseEntity<String> deletePhotos(@PathVariable int id,
            @RequestBody Map<String, List<String>> body,
            HttpServletRequest request) {
        try {
            List<String> deletePictures = body.get("photos");
            System.out.println("Удалить фотографии:" + deletePictures);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();

                if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("USER"))) {
                    carsService.checkCredentials(request, id);
                }
            } else {
                throw new CustomException("Unauthorized", HttpStatus.UNAUTHORIZED);
            }

            if (deletePictures == null || deletePictures.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No pictures specified for deletion");
            }

            CarSQL car = carsService.extractById(id);
            List<String> allPictures = car.getPhoto();
            deletePictures.forEach(picture -> {
                if (allPictures.contains(picture)) {
                    allPictures.remove(picture);
                    commonService.removeAvatar(picture);
                }
            });
            car.setPhoto(allPictures);
            carsService.save(car);

            return ResponseEntity.status(HttpStatus.ACCEPTED).body("Selected photos successfully deleted");
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PostMapping("add-pictures/{id}")
public ResponseEntity<String> patchPhotos(@PathVariable int id,
        @RequestParam("photos") MultipartFile[] newPictures,
        HttpServletRequest request) {
    try {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("USER"))) {
                carsService.checkCredentials(request, id);
            } else {
                throw new CustomException("User is not authorized", HttpStatus.FORBIDDEN);
            }
        } else {
            throw new CustomException("Unauthorized", HttpStatus.UNAUTHORIZED);
        }

        CarSQL carSQL = carsService.extractById(id);

        // Get the existing photo names
        List<String> existingPhotoNames = carSQL.getPhoto();

        // Create a set to track which photos are already on the server
        Set<String> existingPhotosSet = new HashSet<>(existingPhotoNames);

        // Create a list to hold the names of new photos to be added
        List<String> newPhotoNames = new ArrayList<>(existingPhotoNames);

        // Process new pictures
        for (MultipartFile file : newPictures) {
            String fileName = file.getOriginalFilename();
            if (!existingPhotosSet.contains(fileName)) {
                commonService.transferAvatar(file, fileName);
                newPhotoNames.add(fileName);
            }
        }

        // Update the photo list for the car
        carSQL.setPhoto(newPhotoNames);
        carsService.save(carSQL);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Files successfully uploaded");
    } catch (CustomException e) {
        return ResponseEntity.status(e.getStatus()).body(e.getMessage());
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
    }
}


    @PreAuthorize("hasRole('ADMIN', 'USER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteById(@PathVariable int id, HttpServletRequest request) {
        try {
            List<String> pictures = carsService.extractById(id).getPhoto();

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();

                if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("USER"))) {
                    carsService.checkCredentials(request, id);
                }
            } else {
                throw new CustomException("Unauthorized", HttpStatus.UNAUTHORIZED);
            }

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
