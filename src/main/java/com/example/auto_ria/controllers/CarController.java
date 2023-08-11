package com.example.auto_ria.controllers;

import com.example.auto_ria.dto.CarDTO;
import com.example.auto_ria.dto.updateDTO.CarUpdateDTO;
import com.example.auto_ria.enums.EAccountType;
import com.example.auto_ria.enums.ERegion;
import com.example.auto_ria.models.AdministratorSQL;
import com.example.auto_ria.models.CarSQL;
import com.example.auto_ria.models.SellerSQL;
import com.example.auto_ria.models.responses.ErrorResponse;
import com.example.auto_ria.services.CarsServiceMySQLImpl;
import com.example.auto_ria.services.UsersServiceMySQLImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
//            @RequestBody CarDTO car
            @RequestParam("brand") String brand,
            @RequestParam("power") int power,
            @RequestParam("city") String city,
            @RequestParam("region") ERegion region,
            @RequestParam("producer") String producer,
            @RequestParam("price") String price,
            @RequestParam("picture") MultipartFile picture,
            HttpServletRequest request) {
        System.out.println("CAR CONTROLLer");

        SellerSQL seller = usersServiceMySQL.extractSellerFromHeader(request);


        String fileName = picture.getOriginalFilename(); // todo unique name

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
    public ResponseEntity<CarSQL> patchCar(@PathVariable int id,
//                                        @ModelAttribute CarUpdateDTO partialCar,
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

}
