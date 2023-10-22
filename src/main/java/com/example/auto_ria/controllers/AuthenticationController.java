package com.example.auto_ria.controllers;

import com.example.auto_ria.dao.*;
import com.example.auto_ria.enums.ERegion;
import com.example.auto_ria.enums.ERole;
import com.example.auto_ria.enums.ETokenRole;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.models.requests.*;
import com.example.auto_ria.models.responses.AuthenticationResponse;
import com.example.auto_ria.services.*;
import freemarker.template.TemplateException;
import io.jsonwebtoken.Claims;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private PasswordEncoder passwordEncoder;

    // register seller
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

    //activate seller account
    @PostMapping("/activate-seller-account")
    public ResponseEntity<AuthenticationResponse> activateSeller(
            @RequestParam("code") String code
    ) throws MessagingException, TemplateException, IOException {
        if (jwtService.isTokenExprired(code)) {
            throw new CustomException("Activation key expired. Your account has been deleted", HttpStatus.FORBIDDEN);
        }
        String email = jwtService.extractUsername(code, ETokenRole.SELLER_ACTIVATE);
        return authenticationService.activateSeller(email, code);
    }

    // activate seller after banned
//    @PostMapping("/activate-seller")
//    public ResponseEntity<AuthenticationResponse> activateSeller(HttpServletRequest request) {
//        String authorizationHeader = request.getHeader("activation-token");
//        String email = jwtService.extractUsername(authorizationHeader, ETokenRole.SELLER_ACTIVATE);
//
//        return authenticationService.activate(email, ERole.SELLER);
//
//    }

    // generate code for manager register
    @PostMapping("/code-manager")
    public ResponseEntity<String> codeManager(
            @RequestParam("email") String email
    ) throws MessagingException, TemplateException, IOException {
        Map<String, String> claims = new HashMap<>();
        claims.put("email", email);
        claims.put("role", ETokenRole.MANAGER_REGISTER.name());

        String code = jwtService.generateRegistrationCode(claims, email, ETokenRole.MANAGER_REGISTER);

        return authenticationService.codeManager(email, code);
    }

    //register manager with received code
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

    //generate code for admin register
    @PostMapping("/code-admin")
    public ResponseEntity<String> codeAdmin(
            @RequestParam("email") String email
    ) throws MessagingException, TemplateException, IOException {
        Map<String, String> claims = new HashMap<>();
        claims.put("email", email);
        claims.put("role", ETokenRole.ADMIN_REGISTER.name());

        String code = jwtService.generateRegistrationCode(claims, email, ETokenRole.ADMIN_REGISTER);

        return authenticationService.codeAdmin(email, code);
    }

    // register admin with received code
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

    //register customer
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

    // activate customer account with received code
    @PostMapping("/activate-customer-account")
    public ResponseEntity<AuthenticationResponse> activateCustomer(
            @RequestParam("code") String code
    ) throws MessagingException, TemplateException, IOException {
        if (jwtService.isTokenExprired(code)) {
            throw new CustomException("Activation key expired. Your account has been deleted", HttpStatus.FORBIDDEN);
        }
        String email = jwtService.extractUsername(code, ETokenRole.CUSTOMER_ACTIVATE);
        return authenticationService.activateCustomer(email, code);
    }

//    @PostMapping("/activate-customer")
//    public ResponseEntity<AuthenticationResponse> activateCustomer(HttpServletRequest request) {
//        String authorizationHeader = request.getHeader("activation-token");
//        String email = jwtService.extractUsername(authorizationHeader, ETokenRole.CUSTOMER_ACTIVATE);
//        return authenticationService.activate(email, ERole.CUSTOMER);
//    }

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

    @PostMapping("/sign-out")
    public void signOut(
            HttpServletRequest request) {
        String access_token = jwtService.extractTokenFromHeader(request);

        Claims claims = jwtService.extractClaimsCycle(access_token);
        System.out.println(claims);
        String email = claims.get("sub").toString();
        String owner = claims.get("iss").toString();

        authenticationService.signOut(email, owner);
    }

    //todo forgot password - reset password

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(
            @RequestParam("newPassword") String newPassword,
            HttpServletRequest request) {

        if (!newPassword.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$")) {
            throw new CustomException("Invalid password. Must contain: " +
                    "uppercase letter, lowercase letter, number, special character. At least 8 characters long",
                    HttpStatus.BAD_REQUEST);
        }
        String encoded = passwordEncoder.encode(newPassword);

        String accessToken = jwtService.extractTokenFromHeader(request);
        Claims claims = jwtService.extractClaimsCycle(accessToken);

        String email = claims.get("sub").toString();
        String owner = claims.get("iss").toString();

        authenticationService.resetPassword(email, owner, encoded);

        return ResponseEntity.ok("The password has been successfully changed");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(
            @RequestParam("email") String email) throws MessagingException, TemplateException, IOException {

        authenticationService.forgotPassword(email);

        return ResponseEntity.ok("Check" + email.replaceAll(".(?=.{4})", "*"));
    }

    @PostMapping("/out")
    public ResponseEntity<String> signout(
            @RequestParam("email") String email) throws MessagingException, TemplateException, IOException {

        authenticationService.logout(email);

        return ResponseEntity.ok("Check" + email.replaceAll(".(?=.{4})", "*"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @RequestParam("newPassword") String newPassword,
            HttpServletRequest request) {

        if (newPassword.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$")) {
            throw new CustomException("Invalid password. Must contain: " +
                    "uppercase letter, lowercase letter, number, special character. At least 8 characters long",
                    HttpStatus.BAD_REQUEST);
        }
        String accessToken = jwtService.extractTokenFromHeader(request);
        String encoded = passwordEncoder.encode(newPassword);

        Claims claims = jwtService.extractClaimsCycle(accessToken);
        String email = claims.get("email").toString();
        String owner = claims.get("iss").toString();

        if (ERole.ADMIN.equals(ERole.valueOf(owner))) {
            authenticationService.checkRegistrationKey(request, email, ETokenRole.ADMIN);
        } else if (ERole.MANAGER.equals(ERole.valueOf(owner))) {
            authenticationService.checkRegistrationKey(request, email, ETokenRole.MANAGER);
        } else if (ERole.SELLER.equals(ERole.valueOf(owner))) {
            authenticationService.checkRegistrationKey(request, email, ETokenRole.SELLER);
        } else if (ERole.CUSTOMER.equals(ERole.valueOf(owner))) {
            authenticationService.checkRegistrationKey(request, email, ETokenRole.CUSTOMER);
        } else {
            throw new CustomException("Token is invalid for current procedure", HttpStatus.FORBIDDEN);
        }

        authenticationService.resetPassword(email, owner, encoded);

        authenticationService.signOut(email, owner);

        return ResponseEntity.ok("Password has been changed");

    }

}
