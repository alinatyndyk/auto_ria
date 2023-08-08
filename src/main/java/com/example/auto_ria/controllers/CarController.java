package com.example.auto_ria.controllers;

import com.example.auto_ria.dto.CarDTO;
import com.example.auto_ria.dto.CarUpdateDTO;
import com.example.auto_ria.enums.ERegion;
import com.example.auto_ria.models.Car;
import com.example.auto_ria.models.SellerSQL;
import com.example.auto_ria.models.responses.ErrorResponse;
import com.example.auto_ria.services.CarsService;
import com.example.auto_ria.services.UsersServiceMySQLImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(value = "cars")
public class CarController {

    private CarsService carsService;
    private UsersServiceMySQLImpl usersServiceMySQL;

    @GetMapping()
//    @JsonView(ViewsCar.SL3.class)
    public ResponseEntity<List<Car>> getAll() {
        return carsService.getAll();
    }

    @GetMapping("/brands/{brand}")
//    @JsonView(ViewsCar.SL2.class)
    public ResponseEntity<List<Car>> getByBrand(@PathVariable("brand") String brand) {
        return carsService.getByBrand(brand);
    }

    @GetMapping("/power/{power}")
//    @JsonView(ViewsCar.SL2.class)
    public ResponseEntity<List<Car>> getByPower(@PathVariable("power") int power) {
        return carsService.getByPower(power);
    } // todo remove

    @GetMapping("/{id}")
//    @JsonView(ViewsCar.SL1.class)
    public ResponseEntity<Car> getById(@PathVariable("id") int id) {
        return carsService.getById(id);
    }

    @SneakyThrows
    @PostMapping()
    public ResponseEntity<Car> post(
//            @RequestBody CarDTO car
            @RequestParam("brand") String brand,
            @RequestParam("power") int power,
            @RequestParam("city") String city,
            @RequestParam("region") ERegion region,
            @RequestParam("producer") String producer,
            @RequestParam("price") String price,
            @RequestParam("picture") MultipartFile picture,
            HttpServletRequest request) {

        SellerSQL seller = usersServiceMySQL.extractSellerFromHeader(request);

        String fileName = picture.getOriginalFilename() + new Date(); // todo unique name

        CarDTO car = CarDTO
                .builder()
                .brand(brand)
                .powerH(power)
                .city(city)
                .region(region)
                .producer(producer)
                .price(price)
                .photo(fileName)
                .build();


        usersServiceMySQL.transferAvatar(picture, fileName);

        return carsService.post(car, seller);
    }

    @SneakyThrows
    @PatchMapping("/{id}")
    public ResponseEntity<Car> patchCar(@PathVariable int id,
                                        @ModelAttribute CarUpdateDTO partialCar,
                                        HttpServletRequest request) {
//todo transfer album
        SellerSQL seller = usersServiceMySQL.extractSellerFromHeader(request);
        return carsService.update(id, partialCar, seller);
    }


    @DeleteMapping()
    public ResponseEntity<List<Car>> deleteById(@RequestParam("id") int id, HttpServletRequest request) throws ErrorResponse {
        SellerSQL seller = usersServiceMySQL.extractSellerFromHeader(request);
        return carsService.deleteById(id, seller);
    }

}
