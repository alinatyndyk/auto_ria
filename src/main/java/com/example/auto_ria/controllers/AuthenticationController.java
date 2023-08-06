package com.example.auto_ria.controllers;

import com.example.auto_ria.enums.ERegion;
import com.example.auto_ria.enums.ESeller;
import com.example.auto_ria.models.requests.LoginRequest;
import com.example.auto_ria.models.requests.RefreshRequest;
import com.example.auto_ria.models.requests.RegisterRequest;
import com.example.auto_ria.models.responses.AuthenticationResponse;
import com.example.auto_ria.services.AuthenticationService;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthenticationController {

    private AuthenticationService authenticationService;
//    private UsersServiceMySQLImpl usersServiceMySQL;

    @PostMapping("/register-seller")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestParam("name") String name,
            @RequestParam("lastName") String lastName,
            @RequestParam("city") String city,
            @RequestParam("region") ERegion region,
            @RequestParam("avatar") MultipartFile picture,
            @RequestParam("email") String email,
            @RequestParam("number") String number,
            @RequestParam("password") String password,
            @RequestParam("type") ESeller type
    ) {
        String fileName = picture.getOriginalFilename();

//        File transferDestinationFile = usersServiceMySQL.transferAvatar(picture, fileName); // todo transfer mysql users service impl

            RegisterRequest registerRequest = new RegisterRequest(name, lastName, city, region, email, number, fileName, type, password);

        return ResponseEntity.ok(authenticationService.register(registerRequest));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authenticationService.login(loginRequest));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refresh(@RequestBody RefreshRequest refreshRequest) {
        return ResponseEntity.ok(authenticationService.refresh(refreshRequest));
    }
}
