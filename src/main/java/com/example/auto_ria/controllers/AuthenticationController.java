package com.example.auto_ria.controllers;

import com.example.auto_ria.enums.ERegion;
import com.example.auto_ria.models.requests.*;
import com.example.auto_ria.models.responses.AuthenticationResponse;
import com.example.auto_ria.services.AuthenticationService;
import com.example.auto_ria.services.UsersServiceMySQLImpl;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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
    ) throws IOException, MessagingException, TemplateException {
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
            @RequestParam("password") String password
    ) throws IOException {
        String fileName = picture.getOriginalFilename();
        usersServiceMySQL.transferAvatar(picture, fileName);
        RegisterManagerRequest registerRequest = new RegisterManagerRequest(name, email, fileName, password);
        return ResponseEntity.ok(authenticationService.registerManager(registerRequest));
    }

    @PostMapping("/register-admin")
    public ResponseEntity<AuthenticationResponse> registerAdmin(
            @RequestParam("name") String name,
            @RequestParam("lastName") String lastName,
            @RequestParam("avatar") MultipartFile picture,
            @RequestParam("email") String email,
            @RequestParam("password") String password
    ) throws IOException {
        String fileName = picture.getOriginalFilename();
        usersServiceMySQL.transferAvatar(picture, fileName);
        RegisterAdminRequest registerRequest = new RegisterAdminRequest(name, lastName, email, fileName, password);
        return ResponseEntity.ok(authenticationService.registerAdmin(registerRequest));
    }

    @PostMapping("/register-customer")
    public ResponseEntity<AuthenticationResponse> registerCustomer(
            @RequestParam("name") String name,
            @RequestParam("lastName") String lastName,
            @RequestParam("avatar") MultipartFile picture,
            @RequestParam("email") String email,
            @RequestParam("password") String password
    ) throws IOException {
        String fileName = picture.getOriginalFilename();
        usersServiceMySQL.transferAvatar(picture, fileName);
        RegisterAdminRequest registerRequest = new RegisterAdminRequest(name, lastName, email, fileName, password);
        return ResponseEntity.ok(authenticationService.registerCustomer(registerRequest));
    }

    @PostMapping("/authenticate/seller")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authenticationService.login(loginRequest));
    }

    @PostMapping("/authenticate/manager")
    public ResponseEntity<AuthenticationResponse> loginManager(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authenticationService.loginManager(loginRequest));
    }

    @PostMapping("/authenticate/admin")
    public ResponseEntity<AuthenticationResponse> loginAdmin(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authenticationService.loginAdmin(loginRequest));
    }

    @PostMapping("/authenticate/customer")
    public ResponseEntity<AuthenticationResponse> loginCustomer(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authenticationService.loginCustomer(loginRequest));
    }

    @PostMapping("/refresh/seller")
    public ResponseEntity<AuthenticationResponse> refresh(@RequestBody RefreshRequest refreshRequest) {
        return ResponseEntity.ok(authenticationService.refresh(refreshRequest));
    }

    @PostMapping("/refresh/manager")
    public ResponseEntity<AuthenticationResponse> refreshManager(@RequestBody RefreshRequest refreshRequest) {
        return ResponseEntity.ok(authenticationService.refreshManager(refreshRequest));
    }

    @PostMapping("/refresh/admin")
    public ResponseEntity<AuthenticationResponse> refreshAdmin(@RequestBody RefreshRequest refreshRequest) {
        return ResponseEntity.ok(authenticationService.refreshAdmin(refreshRequest));
    }

    @PostMapping("/refresh/customer")
    public ResponseEntity<AuthenticationResponse> refreshCustomer(@RequestBody RefreshRequest refreshRequest) {
        return ResponseEntity.ok(authenticationService.refreshCustomer(refreshRequest));
    }

    //todo forgot password - reset password
}
