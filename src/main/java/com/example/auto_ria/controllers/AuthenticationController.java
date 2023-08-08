package com.example.auto_ria.controllers;

import com.example.auto_ria.enums.ERegion;
import com.example.auto_ria.models.requests.RegisterManagerRequest;
import com.example.auto_ria.models.requests.LoginRequest;
import com.example.auto_ria.models.requests.RefreshRequest;
import com.example.auto_ria.models.requests.RegisterRequest;
import com.example.auto_ria.models.responses.AuthenticationResponse;
import com.example.auto_ria.services.AuthenticationService;
import com.example.auto_ria.services.UsersServiceMySQLImpl;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthenticationController {

    private AuthenticationService authenticationService;
    private UsersServiceMySQLImpl usersServiceMySQL;

    @PostMapping("/register-seller/person")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestParam("name") String name,
            @RequestParam("lastName") String lastName,
            @RequestParam("city") String city,
            @RequestParam("region") ERegion region,
            @RequestParam("avatar") MultipartFile picture,
            @RequestParam("email") String email,
            @RequestParam("number") String number,
            @RequestParam("password") String password
    ) {
        String fileName = picture.getOriginalFilename();

        usersServiceMySQL.transferAvatar(picture, fileName);

        RegisterRequest registerRequest = new RegisterRequest(name, lastName, city, region, email, number, fileName, password);

        return ResponseEntity.ok(authenticationService.register(registerRequest));
    }

    @PostMapping("/register-manager")
    public ResponseEntity<AuthenticationResponse> registerManager(
            @RequestParam("name") String name,
            @RequestParam("avatar") MultipartFile picture,
            @RequestParam("email") String email,
            @RequestParam("password") String password // todo try req body @attribute
    ) {
        String fileName = picture.getOriginalFilename();

        usersServiceMySQL.transferAvatar(picture, fileName);

        RegisterManagerRequest registerRequest = new RegisterManagerRequest(name, email, fileName, password);

        return ResponseEntity.ok(authenticationService.registerManager(registerRequest));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody LoginRequest loginRequest) {
        System.out.println("62");
        return ResponseEntity.ok(authenticationService.login(loginRequest));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refresh(@RequestBody RefreshRequest refreshRequest) {
        return ResponseEntity.ok(authenticationService.refresh(refreshRequest));
    }
}
