package com.example.auto_ria.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.auto_ria.dao.auth.AdminAuthDaoSQL;
import com.example.auto_ria.enums.ERole;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.models.auth.AuthSQL;
import com.example.auto_ria.models.user.UserSQL;
import com.example.auto_ria.services.auth.JwtService;
import com.example.auto_ria.services.user.UsersServiceMySQLImpl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping(value = "common")
@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)

public class CommonController {

    private UsersServiceMySQLImpl usersServiceMySQL;
    private AdminAuthDaoSQL adminAuthDaoSQL;

    private JwtService jwtService;

    @SuppressWarnings("rawtypes")
    @GetMapping("users/by-token")
    public ResponseEntity getByToken(HttpServletRequest request) {
        try {

            String token = jwtService.extractTokenFromHeader(request);

            AuthSQL authSQL = adminAuthDaoSQL.findByAccessToken(token);

            int id = authSQL.getPersonId();

            return ResponseEntity.ok(usersServiceMySQL.getByIdAsResponse(id));

        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        } catch (Exception e) {
            throw new CustomException("Could not retrieve user", HttpStatus.EXPECTATION_FAILED);
        }
    }

}
