package com.example.auto_ria.controllers;

import com.example.auto_ria.exceptions.CustomException;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping(value = "common")
public class CommonController {

    private ManagerServiceMySQL managerServiceMySQL;
    private UsersServiceMySQLImpl usersServiceMySQL;
    private AdministratorServiceMySQL administratorServiceMySQL;
    private CustomersServiceMySQL customersServiceMySQL;

    @PostMapping("users/{id}")
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

}
