package com.example.auto_ria.controllers;

import com.example.auto_ria.dao.auth.AdminAuthDaoSQL;
import com.example.auto_ria.dao.auth.CustomerAuthDaoSQL;
import com.example.auto_ria.dao.auth.ManagerAuthDaoSQL;
import com.example.auto_ria.dao.auth.SellerAuthDaoSQL;
import com.example.auto_ria.dao.user.AdministratorDaoSQL;
import com.example.auto_ria.dao.user.CustomerDaoSQL;
import com.example.auto_ria.dao.user.ManagerDaoSQL;
import com.example.auto_ria.dao.user.UserDaoSQL;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.models.auth.AuthSQL;
import com.example.auto_ria.models.responses.user.CustomerResponse;
import com.example.auto_ria.models.user.AdministratorSQL;
import com.example.auto_ria.models.user.CustomerSQL;
import com.example.auto_ria.models.user.ManagerSQL;
import com.example.auto_ria.models.user.SellerSQL;
import com.example.auto_ria.services.CommonService;
import com.example.auto_ria.services.auth.JwtService;
import com.example.auto_ria.services.user.AdministratorServiceMySQL;
import com.example.auto_ria.services.user.CustomersServiceMySQL;
import com.example.auto_ria.services.user.ManagerServiceMySQL;
import com.example.auto_ria.services.user.UsersServiceMySQLImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@AllArgsConstructor
@RequestMapping(value = "common")
@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)

public class CommonController {

    private ManagerServiceMySQL managerServiceMySQL;
    private UsersServiceMySQLImpl usersServiceMySQL;
    private AdministratorServiceMySQL administratorServiceMySQL;
    private CustomersServiceMySQL customersServiceMySQL;

    private ManagerDaoSQL managerDaoSQL;
    private UserDaoSQL sellerDaoSQL;
    private AdministratorDaoSQL adminDaoSQL;
    private CustomerDaoSQL customerDaoSQL;

    private ManagerAuthDaoSQL managerAuthDaoSQL;
    private SellerAuthDaoSQL sellerAuthDaoSQL;
    private AdminAuthDaoSQL adminAuthDaoSQL;
    private CustomerAuthDaoSQL customerAuthDaoSQL;

    private JwtService jwtService;

    @GetMapping("users/{id}")
    public ResponseEntity getIdAll(@PathVariable String id) {
        try {

            //todo change int parse
            CustomerSQL customerSQL = customersServiceMySQL.getById(id).getBody();
            ManagerSQL managerSQL = managerServiceMySQL.getById(Integer.parseInt(id)).getBody();
            AdministratorSQL administratorSQL = administratorServiceMySQL.getById(id).getBody();
            SellerSQL sellerSQL = usersServiceMySQL.getById(id).getBody();

            if (customerSQL != null) {
                return ResponseEntity.ok(customerSQL);
            } else if (managerSQL != null) {
                return ResponseEntity.ok(managerSQL);
            } else if (administratorSQL != null) {
                return ResponseEntity.ok(administratorSQL);
            } else if (sellerSQL != null) {
                return ResponseEntity.ok(sellerSQL);
            } else {
                throw new CustomException("No users found", HttpStatus.BAD_REQUEST);
            }
        } catch (
                CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }

    }

    @GetMapping("users/by-token")
    public ResponseEntity getByToken(HttpServletRequest request) {
        try {
            System.out.println("87");

            String token = jwtService.extractTokenFromHeader(request);

            //todo change int parse, authInfoEntity delete
            AuthSQL authSQL = adminAuthDaoSQL.findByAccessToken(token);
            int id = authSQL.getPersonId();
            System.out.println("94");

            Optional<CustomerSQL> customerSQL = customerDaoSQL.findById(id);
            System.out.println("97");
            Optional<ManagerSQL> managerSQL = managerDaoSQL.findById(id);
            Optional<AdministratorSQL> administratorSQL = adminDaoSQL.findById(id); //todo stop search when found
            Optional<SellerSQL> sellerSQL = sellerDaoSQL.findById(id);
            System.out.println("100");

            if (customerSQL.isPresent()) {
                return ResponseEntity.ok(customersServiceMySQL.getByIdAsResponse(id));
            } else if (managerSQL.isPresent()) {
                return ResponseEntity.ok(managerServiceMySQL.getByIdAsResponse(id));
            } else if (administratorSQL.isPresent()) {
                return ResponseEntity.ok(administratorServiceMySQL.getByIdAsResponse(id)); //fix
            } else if (sellerSQL.isPresent()) {
                return ResponseEntity.ok(usersServiceMySQL.getByIdAsResponse(id));
            } else {
                throw new CustomException("No users found", HttpStatus.BAD_REQUEST);
            }
        } catch (
                CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

}
