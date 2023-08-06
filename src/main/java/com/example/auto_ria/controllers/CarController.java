package com.example.auto_ria.controllers;

import com.example.auto_ria.dao.UserDaoSQL;
import com.example.auto_ria.dto.CarDTO;
import com.example.auto_ria.dto.CarUpdateDTO;
import com.example.auto_ria.models.Car;
import com.example.auto_ria.models.Seller;
import com.example.auto_ria.models.UserSQL;
import com.example.auto_ria.services.CarsService;
import com.example.auto_ria.services.JwtService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(value = "cars")
public class CarController {

    private CarsService carsService;
    private UserDaoSQL userDaoSQL; //todo create a separate controller and service!!!
    private JwtService jwtService;

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
    }

    @GetMapping("/{id}")
//    @JsonView(ViewsCar.SL1.class)
    public ResponseEntity<Car> getById(@PathVariable("id") int id) {
        return carsService.getById(id);
    }

    @PostMapping()
//    @Secured("SELLER")
    public ResponseEntity<Car> post(@RequestBody CarDTO car) {
        System.out.println("Post car");
        return carsService.post(car);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Car> patchCar(@PathVariable int id, @RequestBody CarUpdateDTO partialCar, HttpServletRequest request) throws IllegalAccessException {

        String bearerToken = null;

        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            bearerToken = authorizationHeader.substring(7);
        }

        String email = jwtService.extractUsername(bearerToken);

        Seller seller = userDaoSQL.findSellerByEmail(email);

        // todo check if the car belongs to the user

        return carsService.update(id, partialCar);
    }


    @DeleteMapping()
    public ResponseEntity<List<Car>> deleteById(@RequestParam("id") int id) {
        return carsService.deleteById(id);
    }

}
