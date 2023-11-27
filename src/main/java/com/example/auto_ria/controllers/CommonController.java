package com.example.auto_ria.controllers;

import com.example.auto_ria.dao.auth.AdminAuthDaoSQL;
import com.example.auto_ria.enums.ERole;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.models.auth.AuthSQL;
import com.example.auto_ria.models.user.AdministratorSQL;
import com.example.auto_ria.models.user.CustomerSQL;
import com.example.auto_ria.models.user.ManagerSQL;
import com.example.auto_ria.models.user.SellerSQL;
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

@RestController
@AllArgsConstructor
@RequestMapping(value = "common")
@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)

public class CommonController {

    private ManagerServiceMySQL managerServiceMySQL;
    private UsersServiceMySQLImpl usersServiceMySQL;
    private AdministratorServiceMySQL administratorServiceMySQL;
    private CustomersServiceMySQL customersServiceMySQL;
    private AdminAuthDaoSQL adminAuthDaoSQL;

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

            String token = jwtService.extractTokenFromHeader(request);

            AuthSQL authSQL = adminAuthDaoSQL.findByAccessToken(token);
            int id = authSQL.getPersonId();
            ERole role = authSQL.getRole();

            if (role.equals(ERole.CUSTOMER)) {
                return ResponseEntity.ok(customersServiceMySQL.getByIdAsResponse(id));
            } else if (role.equals(ERole.MANAGER)) {
                return ResponseEntity.ok(managerServiceMySQL.getByIdAsResponse(id));
            } else if (role.equals(ERole.ADMIN)) {
                return ResponseEntity.ok(administratorServiceMySQL.getByIdAsResponse(id)); //fix
            } else if (role.equals(ERole.SELLER)) {
                return ResponseEntity.ok(usersServiceMySQL.getByIdAsResponse(id));
            } else {
                throw new CustomException("No users found", HttpStatus.BAD_REQUEST);
            }
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        } catch (Exception e) {
            throw new CustomException("Could not retrieve user", HttpStatus.EXPECTATION_FAILED);
        }
    }

}
