package com.example.auto_ria.controllers;

import com.example.auto_ria.dao.RegisterKeyDaoSQL;
import com.example.auto_ria.enums.ERegion;
import com.example.auto_ria.enums.ERole;
import com.example.auto_ria.enums.ETokenRole;
import com.example.auto_ria.models.requests.*;
import com.example.auto_ria.models.responses.AuthenticationResponse;
import com.example.auto_ria.services.AuthenticationService;
import com.example.auto_ria.services.JwtService;
import com.example.auto_ria.services.UsersServiceMySQLImpl;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthenticationController {

    private AuthenticationService authenticationService;
    private UsersServiceMySQLImpl usersServiceMySQL;
    private RegisterKeyDaoSQL registerKeyDaoSQL;
    private JwtService jwtService;

    @PostMapping("/register-seller/person")
    public ResponseEntity<String> register(
            @RequestParam("name") String name,
            @RequestParam("lastName") String lastName,
            @RequestParam("city") String city,
            @RequestParam("region") ERegion region,
            @RequestParam("avatar") MultipartFile picture,
            @RequestParam("email") String email,
            @RequestParam("number") String number,
            @RequestParam("password") String password
    ) throws IOException {
        String fileName = picture.getOriginalFilename();
        usersServiceMySQL.transferAvatar(picture, fileName);
        RegisterRequest registerRequest = new RegisterRequest(city, region, number, name, lastName, email, fileName, password);
        return authenticationService.register(registerRequest);
    }

    @PostMapping("/code-manager")
    public ResponseEntity<String> codeManager(
            @RequestParam("email") String email
    ) throws MessagingException, TemplateException, IOException {
        Map<String, String> claims = new HashMap<>();
        claims.put("email", email);
        claims.put("role", ETokenRole.MANAGER_REGISTER.name());

        String code = jwtService.generateManagerCode(claims, email);

        return authenticationService.codeManager(email, code);
    }

    @PostMapping("/register-manager")
    public ResponseEntity<AuthenticationResponse> registerManager(
            @RequestParam("name") String name,
            @RequestParam("avatar") MultipartFile picture,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            HttpServletRequest request
    ) throws IOException {

        String key = authenticationService.checkRegistrationKey(request, email, ETokenRole.MANAGER_REGISTER);

        String fileName = picture.getOriginalFilename();
        usersServiceMySQL.transferAvatar(picture, fileName);
        RegisterManagerRequest registerRequest = new RegisterManagerRequest(name, email, fileName, password);
        registerKeyDaoSQL.delete(registerKeyDaoSQL.findByRegisterKey(key));
        return ResponseEntity.ok(authenticationService.registerManager(registerRequest, key));
    }

    @PostMapping("/activate-seller")
    public ResponseEntity<AuthenticationResponse> activateSeller(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("activation-token");
        String email = jwtService.extractUsername(authorizationHeader, ETokenRole.SELLER_ACTIVATE);

        return authenticationService.activate(email, ERole.SELLER);

    }

    @PostMapping("/activate-customer")
    public ResponseEntity<AuthenticationResponse> activateCustomer(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("activation-token");
        String email = jwtService.extractUsername(authorizationHeader, ETokenRole.CUSTOMER_ACTIVATE);
        return authenticationService.activate(email, ERole.CUSTOMER);
    }

    @PostMapping("/code-admin")
    public ResponseEntity<String> codeAdmin(
            @RequestParam("email") String email
    ) throws MessagingException, TemplateException, IOException {
        Map<String, String> claims = new HashMap<>();
        claims.put("email", email);
        claims.put("role", ETokenRole.ADMIN_REGISTER.name());

        String code = jwtService.generateAdminCode(claims, email);

        return authenticationService.codeAdmin(email, code);
    }

    @PostMapping("/register-admin")
    public ResponseEntity<AuthenticationResponse> registerAdmin(
            @RequestParam("name") String name,
            @RequestParam("lastName") String lastName,
            @RequestParam("avatar") MultipartFile picture,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            HttpServletRequest request
    ) throws IOException {

        String key = authenticationService.checkRegistrationKey(request, email, ETokenRole.ADMIN_REGISTER);

        String fileName = picture.getOriginalFilename();
        usersServiceMySQL.transferAvatar(picture, fileName);
        RegisterAdminRequest registerRequest = new RegisterAdminRequest(name, lastName, email, fileName, password);
        AuthenticationResponse authenticationResponse = authenticationService.registerAdmin(registerRequest);
        registerKeyDaoSQL.delete(registerKeyDaoSQL.findByRegisterKey(key));
        return ResponseEntity.ok(authenticationResponse);
    }

    @PostMapping("/register-customer")
    public ResponseEntity<String> registerCustomer(
            @RequestParam("name") String name,
            @RequestParam("lastName") String lastName,
            @RequestParam("avatar") MultipartFile picture,
            @RequestParam("email") String email,
            @RequestParam("password") String password
    ) throws IOException {
        String fileName = picture.getOriginalFilename();
        usersServiceMySQL.transferAvatar(picture, fileName);
        RegisterAdminRequest registerRequest = new RegisterAdminRequest(name, lastName, email, fileName, password);
        return authenticationService.registerCustomer(registerRequest);
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
    public ResponseEntity<AuthenticationResponse> refresh(@RequestBody @Valid RefreshRequest refreshRequest) {
        return ResponseEntity.ok(authenticationService.refresh(refreshRequest));
    }

    @PostMapping("/refresh/manager")
    public ResponseEntity<AuthenticationResponse> refreshManager(@RequestBody @Valid RefreshRequest refreshRequest) {
        return ResponseEntity.ok(authenticationService.refreshManager(refreshRequest));
    }

    @PostMapping("/refresh/admin")
    public ResponseEntity<AuthenticationResponse> refreshAdmin(@RequestBody @Valid RefreshRequest refreshRequest) {
        return ResponseEntity.ok(authenticationService.refreshAdmin(refreshRequest));
    }

    @PostMapping("/refresh/customer")
    public ResponseEntity<AuthenticationResponse> refreshCustomer(@RequestBody @Valid RefreshRequest refreshRequest) {
        return ResponseEntity.ok(authenticationService.refreshCustomer(refreshRequest));
    }

    //todo forgot password - reset password
}
