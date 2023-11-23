package com.example.auto_ria.controllers;

import com.example.auto_ria.dao.auth.AdminAuthDaoSQL;
import com.example.auto_ria.dao.auth.CustomerAuthDaoSQL;
import com.example.auto_ria.dao.auth.ManagerAuthDaoSQL;
import com.example.auto_ria.dao.auth.SellerAuthDaoSQL;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.models.auth.AuthSQL;
import com.example.auto_ria.models.responses.user.CustomerResponse;
import com.example.auto_ria.models.user.AdministratorSQL;
import com.example.auto_ria.models.user.CustomerSQL;
import com.example.auto_ria.models.user.ManagerSQL;
import com.example.auto_ria.models.user.SellerSQL;
import com.example.auto_ria.services.user.AdministratorServiceMySQL;
import com.example.auto_ria.services.user.CustomersServiceMySQL;
import com.example.auto_ria.services.user.ManagerServiceMySQL;
import com.example.auto_ria.services.user.UsersServiceMySQLImpl;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping(value = "common")
@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)

public class CommonController {

    private ManagerServiceMySQL managerServiceMySQL;
    private UsersServiceMySQLImpl usersServiceMySQL;
    private AdministratorServiceMySQL administratorServiceMySQL;
    private CustomersServiceMySQL customersServiceMySQL;

    private ManagerAuthDaoSQL managerAuthDaoSQL;
    private SellerAuthDaoSQL sellerAuthDaoSQL;
    private AdminAuthDaoSQL adminAuthDaoSQL;
    private CustomerAuthDaoSQL customerAuthDaoSQL;

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

    @PostMapping("users}")
    public ResponseEntity getByToken(@RequestParam String token) {
        try {

            //todo change int parse, authInfoEntity delete
            AuthSQL customerSQL = customerAuthDaoSQL.findByAccessToken(token);
            AuthSQL managerSQL = managerAuthDaoSQL.findByAccessToken(token);
            AuthSQL administratorSQL = adminAuthDaoSQL.findByAccessToken(token);
            AuthSQL sellerSQL = sellerAuthDaoSQL.findByAccessToken(token);

            if (customerSQL != null) {
                return ResponseEntity.ok(customersServiceMySQL.getById(String.valueOf(customerSQL.getPersonId())));
            } else if (managerSQL != null) {
                return ResponseEntity.ok(managerServiceMySQL.getById(managerSQL.getPersonId()));
            } else if (administratorSQL != null) {
                return ResponseEntity.ok(administratorServiceMySQL.getById(String.valueOf(administratorSQL.getPersonId())));
            } else if (sellerSQL != null) {
                return ResponseEntity.ok(usersServiceMySQL.getById(String.valueOf(sellerSQL.getPersonId())));
            } else {
                throw new CustomException("No users found", HttpStatus.BAD_REQUEST);
            }
        } catch (
                CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }

    }

}
