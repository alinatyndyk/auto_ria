package com.example.auto_ria.controllers;

import com.example.auto_ria.dto.CarDTO;
import com.example.auto_ria.dto.updateDTO.CarUpdateDTO;
import com.example.auto_ria.enums.EAccountType;
import com.example.auto_ria.enums.ERegion;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.models.AdministratorSQL;
import com.example.auto_ria.models.CarSQL;
import com.example.auto_ria.models.SellerSQL;
import com.example.auto_ria.models.responses.StatisticsResponse;
import com.example.auto_ria.services.CarsServiceMySQLImpl;
import com.example.auto_ria.services.MixpanelService;
import com.example.auto_ria.services.ProfanityFilterService;
import com.example.auto_ria.services.UsersServiceMySQLImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@AllArgsConstructor
@RequestMapping(value = "cars")
public class CarController {

    private CarsServiceMySQLImpl carsService;
    private UsersServiceMySQLImpl usersServiceMySQL;
    private MixpanelService mixpanelService;
    private ProfanityFilterService profanityFilterService;

    private static final AtomicInteger validationFailureCounter = new AtomicInteger(0);


    @GetMapping()
//    @JsonView(ViewsCar.SL3.class)
    public ResponseEntity<List<CarSQL>> getAll() throws IOException {
        return carsService.getAll();
    }

    @PostMapping("/viewed/{id}")
    public void addView(
            @PathVariable("id") int id
    ) {
        mixpanelService.view(String.valueOf(id));
    }

    @GetMapping("seller/{id}")
    public ResponseEntity<List<CarSQL>> getAllBySeller(
            @PathVariable("id") int id) {
        SellerSQL sellerSQL = usersServiceMySQL.getById(id);

        return carsService.getBySeller(sellerSQL);
    }

    @GetMapping("/my-cars")
    public ResponseEntity<List<CarSQL>> getMyCars(HttpServletRequest request) {
        SellerSQL sellerSQL = usersServiceMySQL.extractSellerFromHeader(request);
        return carsService.getBySeller(sellerSQL);
    }

    @GetMapping("/statistics/{id}")
    public ResponseEntity<StatisticsResponse> getStatistics(
            @PathVariable("id") int id,
            HttpServletRequest request) {
        SellerSQL sellerSQL = usersServiceMySQL.extractSellerFromHeader(request);
        AdministratorSQL administratorSQL = usersServiceMySQL.extractAdminFromHeader(request);

        assert administratorSQL == null;
        if (!sellerSQL.getAccountType().equals(EAccountType.PREMIUM)) {
            throw new CustomException("Premium plan required", HttpStatus.PAYMENT_REQUIRED);
        }

        return ResponseEntity.ok(mixpanelService.getCarViewsStatistics(String.valueOf(id)));

    }

    @GetMapping("/{id}")
    public ResponseEntity<CarSQL> getById(@PathVariable("id") int id) {
        return carsService.getById(id);
    }

    @SneakyThrows
    @PostMapping()
    public ResponseEntity<CarSQL> post(
            @RequestParam("brand") String brand,
            @RequestParam("power") int power,
            @RequestParam("city") String city,
            @RequestParam("region") ERegion region,
            @RequestParam("producer") String producer,
            @RequestParam("price") String price,
            @RequestParam("pictures") MultipartFile[] pictures,
            @RequestParam("description") String description,
            HttpServletRequest request) {

        String filteredText = profanityFilterService.containsProfanity(description);
        if (profanityFilterService.containsProfanityBoolean(filteredText, description)) {
            int currentCount = validationFailureCounter.incrementAndGet();

            if (currentCount > 3) {
                throw new CustomException("Validation failed. We will have to check this application. It is not activated.", HttpStatus.NOT_ACCEPTABLE);
            }
            int attemptsLeft = 4 - currentCount;
            throw new CustomException("Consider editing your description. Profanity found: attempts left: " + attemptsLeft, HttpStatus.BAD_REQUEST);
        }

        CarDTO car = CarDTO
                .builder()
                .brand(brand)
                .powerH(power)
                .city(city)
                .region(region)
                .producer(producer)
                .price(price)
                .description(description)
                .build();

        SellerSQL seller = usersServiceMySQL.extractSellerFromHeader(request);

        if (seller.getAccountType().equals(EAccountType.BASIC) && !carsService.getBySellerList(seller).isEmpty()) {
            throw new CustomException("Forbidden. Premium account required", HttpStatus.FORBIDDEN);
        }


        List<String> fileNames = new ArrayList<>();  //todo check album

        Arrays.stream(pictures).map(picture -> {
            String fileName = picture.getOriginalFilename();
            System.out.println(fileName);
            fileNames.add(fileName);
            try {
                usersServiceMySQL.transferAvatar(picture, fileName);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
        car.setPhoto(fileNames);

        return carsService.post(car, seller);
    }

    @SneakyThrows
    @PatchMapping("/{id}")
    public ResponseEntity<CarSQL> patchCar(@PathVariable int id,
//                                       todo  @ModelAttribute CarUpdateDTO partialCar,
                                           @RequestBody CarUpdateDTO partialCar,
                                           HttpServletRequest request) {
//todo transfer album
        SellerSQL sellerFromHeader = usersServiceMySQL.extractSellerFromHeader(request);
        SellerSQL sellerFromCar = carsService.extractById(id).getSeller();

        AdministratorSQL administrator = usersServiceMySQL.extractAdminFromHeader(request);

        if (!sellerFromHeader.equals(sellerFromCar) && administrator == null) {
            throw new CustomException("Access_denied: check credentials", HttpStatus.FORBIDDEN);
        }

        List<CarSQL> cars = carsService.getBySeller(sellerFromHeader).getBody();

        assert cars != null;
        assert administrator != null; // todo check
        if (sellerFromHeader.getAccountType().equals(EAccountType.BASIC) && cars.isEmpty()) {
            throw new CustomException("Forbidden. Basic_account: The car already exists", HttpStatus.PAYMENT_REQUIRED);
        }
        return carsService.update(id, partialCar, sellerFromHeader);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteById(@PathVariable int id, HttpServletRequest request) {
        SellerSQL seller = usersServiceMySQL.extractSellerFromHeader(request);
        AdministratorSQL administrator = usersServiceMySQL.extractAdminFromHeader(request);

        return carsService.deleteById(id, seller, administrator);
    }

}
