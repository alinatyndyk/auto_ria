package com.example.auto_ria.controllers;

import com.example.auto_ria.dto.CarDTO;
import com.example.auto_ria.dto.updateDTO.CarUpdateDTO;
import com.example.auto_ria.enums.EAccountType;
import com.example.auto_ria.enums.ERegion;
import com.example.auto_ria.models.CarSQL;
import com.example.auto_ria.models.SellerSQL;
import com.example.auto_ria.models.responses.ErrorResponse;
import com.example.auto_ria.services.CarsServiceMySQLImpl;
import com.example.auto_ria.services.UsersServiceMySQLImpl;
import com.mixpanel.mixpanelapi.MessageBuilder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(value = "cars")
public class CarController {

    private CarsServiceMySQLImpl carsService;
    private UsersServiceMySQLImpl usersServiceMySQL;

    @GetMapping()
//    @JsonView(ViewsCar.SL3.class)
    public ResponseEntity<List<CarSQL>> getAll() {
        return carsService.getAll();
    }

    @GetMapping("/{id}")
//    @JsonView(ViewsCar.SL1.class)
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
            HttpServletRequest request) {

        SellerSQL seller = usersServiceMySQL.extractSellerFromHeader(request);

        List<CarSQL> cars = carsService.getBySellerList(seller);

        if (seller.getAccountType().equals(EAccountType.BASIC) && !carsService.getBySellerList(seller).isEmpty()) {
            throw new ErrorResponse(403, "Forbidden. Premium account required");
        }

        CarDTO car = CarDTO
                .builder()
                .brand(brand)
                .powerH(power)
                .city(city)
                .region(region)
                .producer(producer)
                .price(price)
                .build();


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
        SellerSQL seller = usersServiceMySQL.extractSellerFromHeader(request);
        List<CarSQL> cars = carsService.getBySeller(seller).getBody();
        assert cars != null;
        if (seller.getAccountType().equals(EAccountType.BASIC) && cars.isEmpty()) {
            throw new ErrorResponse(403, "Forbidden. Basic_account: The car already exists");
        }
        return carsService.update(id, partialCar, seller);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteById(@PathVariable int id, HttpServletRequest request) throws ErrorResponse {
        SellerSQL seller = usersServiceMySQL.extractSellerFromHeader(request);
        return carsService.deleteById(id, seller);
    }

    @PostMapping("/view/{id}")
    public void addView(
            @PathVariable("id") int id) {

    }

}
