package com.example.auto_ria.controllers;

import com.example.auto_ria.dao.RegisterKeyDaoSQL;
import com.example.auto_ria.dto.requests.RegisterRequestAdminDTO;
import com.example.auto_ria.dto.requests.RegisterRequestCustomerDTO;
import com.example.auto_ria.dto.requests.RegisterRequestManagerDTO;
import com.example.auto_ria.dto.requests.RegisterRequestSellerDTO;
import com.example.auto_ria.enums.ERole;
import com.example.auto_ria.enums.ETokenRole;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.models.requests.*;
import com.example.auto_ria.models.responses.AuthenticationResponse;
import com.example.auto_ria.services.*;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthenticationController {

    private AuthenticationService authenticationService;

    private UsersServiceMySQLImpl usersServiceMySQL;
    private AdministratorServiceMySQL administratorServiceMySQL;
    private CustomersServiceMySQL customersServiceMySQL;
    private ManagerServiceMySQL managerServiceMySQL;

    private RegisterKeyDaoSQL registerKeyDaoSQL;
    private JwtService jwtService;
    private PasswordEncoder passwordEncoder;
    private CitiesService citiesService;

    @PostMapping("/register-seller/person")
    public ResponseEntity<String> register(
            @ModelAttribute @Valid RegisterRequestSellerDTO registerRequestDTO,
            @RequestPart("avatar") MultipartFile picture
    ) {
        try {
            citiesService.isValidUkrainianCity(registerRequestDTO.getRegion(), registerRequestDTO.getCity());

            String fileName = picture.getOriginalFilename();
            usersServiceMySQL.transferAvatar(picture, fileName);

            RegisterSellerRequest registerRequest = new RegisterSellerRequest(
                    registerRequestDTO.getCity(),
                    registerRequestDTO.getRegion(),
                    registerRequestDTO.getName(),
                    registerRequestDTO.getLastName(),
                    registerRequestDTO.getEmail(),
                    registerRequestDTO.getNumber(),
                    fileName,
                    registerRequestDTO.getPassword());
            return authenticationService.register(registerRequest);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PostMapping("/activate-seller-account")
    public ResponseEntity<AuthenticationResponse> activateSeller(
            @RequestParam("code") String code
    ) {
        try {
            if (jwtService.isTokenExprired(code)) {
                throw new CustomException("Activation key expired. Your account has been deleted", HttpStatus.FORBIDDEN);
            }
            System.out.println("78");
            String email = jwtService.extractUsername(code, ETokenRole.SELLER_ACTIVATE);
            System.out.println("80");
            return authenticationService.activateSeller(email, code);
        } catch (CustomException e) {
            System.out.println("83");
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PostMapping("/code-manager")
    public ResponseEntity<String> codeManager(
            @RequestParam("email") String email
    ) {
        try {
            Map<String, String> claims = new HashMap<>();
            claims.put("email", email);
            claims.put("role", ETokenRole.MANAGER_REGISTER.name());

            String code = jwtService.generateRegistrationCode(claims, email, ETokenRole.MANAGER_REGISTER);

            return authenticationService.codeManager(email, code);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PostMapping("/register-manager")
    public ResponseEntity<AuthenticationResponse> registerManager(
            @ModelAttribute @Valid RegisterRequestManagerDTO registerRequestDTO,
            @RequestPart("avatar") MultipartFile picture,
            HttpServletRequest request
    ) {
        try {
            String code = request.getHeader("Register-key");

            Claims claims = jwtService.extractClaimsCycle(code);
            String tokenType = claims.get("recognition").toString();

            String key = authenticationService.checkRegistrationKey(
                    code,
                    registerRequestDTO.getEmail(),
                    ERole.MANAGER,
                    ETokenRole.valueOf(tokenType),
                    ETokenRole.MANAGER_REGISTER);

            String fileName = picture.getOriginalFilename();
            usersServiceMySQL.transferAvatar(picture, fileName);

            RegisterManagerRequest registerRequest = new RegisterManagerRequest(
                    registerRequestDTO.getName(),
                    registerRequestDTO.getEmail(),
                    fileName,
                    registerRequestDTO.getPassword(),
                    registerRequestDTO.getLastName());

            registerKeyDaoSQL.delete(registerKeyDaoSQL.findByRegisterKey(key));
            return authenticationService.registerManager(registerRequest, key);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PostMapping("/code-admin")
    public ResponseEntity<String> codeAdmin(
            @RequestParam("email") String email
    ) {
        try {
            Map<String, String> claims = new HashMap<>();
            claims.put("email", email);
            claims.put("role", ETokenRole.ADMIN_REGISTER.name());

            String code = jwtService.generateRegistrationCode(claims, email, ETokenRole.ADMIN_REGISTER);

            return authenticationService.codeAdmin(email, code);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PostMapping("/register-admin")
    public ResponseEntity<AuthenticationResponse> registerAdmin(
            @ModelAttribute @Valid RegisterRequestAdminDTO registerRequestDTO,
            @RequestPart("avatar") MultipartFile picture,
            HttpServletRequest request
    ) {
        try {
            String code = request.getHeader("Register-key");

            Claims claims = jwtService.extractClaimsCycle(code);
            String tokenType = claims.get("recognition").toString();

            String key = authenticationService.checkRegistrationKey(
                    code,
                    registerRequestDTO.getEmail(),
                    ERole.ADMIN,
                    ETokenRole.valueOf(tokenType),
                    ETokenRole.ADMIN_REGISTER);

            String fileName = picture.getOriginalFilename();
            usersServiceMySQL.transferAvatar(picture, fileName);

            RegisterAdminRequest registerRequest = new RegisterAdminRequest(
                    registerRequestDTO.getName(),
                    registerRequestDTO.getLastName(),
                    registerRequestDTO.getEmail(),
                    fileName,
                    registerRequestDTO.getPassword());

            registerKeyDaoSQL.delete(registerKeyDaoSQL.findByRegisterKey(key));

            return authenticationService.registerAdmin(registerRequest);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PostMapping("/register-customer")
    public ResponseEntity<String> registerCustomer(
            @ModelAttribute @Valid RegisterRequestCustomerDTO registerRequestDTO,
            @RequestPart("avatar") MultipartFile picture
    ) {
        try {
            String fileName = picture.getOriginalFilename();
            usersServiceMySQL.transferAvatar(picture, fileName);
            RegisterCustomerRequest registerRequest = new RegisterCustomerRequest(
                    registerRequestDTO.getCity(),
                    registerRequestDTO.getRegion(),
                    registerRequestDTO.getName(),
                    registerRequestDTO.getLastName(),
                    registerRequestDTO.getEmail(),
                    fileName,
                    registerRequestDTO.getPassword());
            return authenticationService.registerCustomer(registerRequest);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PostMapping("/activate-customer-account")
    public ResponseEntity<AuthenticationResponse> activateCustomer(
            @RequestParam("code") String code
    ) {
        try {
            if (jwtService.isTokenExprired(code)) {
                throw new CustomException("Activation key expired. Your account has been deleted", HttpStatus.FORBIDDEN);
            }
            String email = jwtService.extractUsername(code, ETokenRole.CUSTOMER_ACTIVATE);
            return authenticationService.activateCustomer(email, code);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PostMapping("/authenticate/seller")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody LoginRequest loginRequest) {
        try {
            if (!usersServiceMySQL.getByEmail(loginRequest.getEmail()).getIsActivated().equals(true)) {
                throw new CustomException("Account is inactivated", HttpStatus.FORBIDDEN);
            }
            return ResponseEntity.ok(authenticationService.login(loginRequest));
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PostMapping("/authenticate/manager")
    public ResponseEntity<AuthenticationResponse> loginManager(@RequestBody LoginRequest loginRequest) {
        try {
            if (!managerServiceMySQL.getByEmail(loginRequest.getEmail()).getIsActivated().equals(true)) {
                throw new CustomException("Account is inactivated", HttpStatus.FORBIDDEN);
            }
            return ResponseEntity.ok(authenticationService.loginManager(loginRequest));
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PostMapping("/authenticate/admin")
    public ResponseEntity<AuthenticationResponse> loginAdmin(@RequestBody LoginRequest loginRequest) {
        try {
            System.out.println("256");
            if (!administratorServiceMySQL.getByEmail(loginRequest.getEmail()).getIsActivated().equals(true)) {
                throw new CustomException("Account is inactivated", HttpStatus.FORBIDDEN);
            }
            System.out.println("260");
            return ResponseEntity.ok(authenticationService.loginAdmin(loginRequest));
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PostMapping("/authenticate/customer")
    public ResponseEntity<AuthenticationResponse> loginCustomer(@RequestBody LoginRequest loginRequest) {
        try {
            if (!customersServiceMySQL.getByEmail(loginRequest.getEmail()).getIsActivated().equals(true)) {
                throw new CustomException("Account is inactivated", HttpStatus.FORBIDDEN);
            }
            return ResponseEntity.ok(authenticationService.loginCustomer(loginRequest));
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PostMapping("/refresh/seller")
    public ResponseEntity<AuthenticationResponse> refresh(@RequestBody @Valid RefreshSellerRequest refreshRequest) {
        try {
            return ResponseEntity.ok(authenticationService.refresh(refreshRequest));
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PostMapping("/refresh/manager")
    public ResponseEntity<AuthenticationResponse> refreshManager(@RequestBody @Valid RefreshSellerRequest refreshRequest) {
        try {
            return ResponseEntity.ok(authenticationService.refreshManager(refreshRequest));
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PostMapping("/refresh/admin")
    public ResponseEntity<AuthenticationResponse> refreshAdmin(@RequestBody @Valid RefreshSellerRequest refreshRequest) {
        try {
            return ResponseEntity.ok(authenticationService.refreshAdmin(refreshRequest));
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PostMapping("/refresh/customer")
    public ResponseEntity<AuthenticationResponse> refreshCustomer(@RequestBody @Valid RefreshSellerRequest refreshRequest) {
        try {
            return ResponseEntity.ok(authenticationService.refreshCustomer(refreshRequest));
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PostMapping("/change-passwords")
    public ResponseEntity<String> changePassword(
            @RequestParam("newPassword") String newPassword,
            HttpServletRequest request) {
        try {
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
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(
            @RequestParam("email") String email) {
        try {

            authenticationService.forgotPassword(email);

            return ResponseEntity.ok("Check" + email.replaceAll(".(?=.{8})", "*"));
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PostMapping("/sign-out")
    public ResponseEntity<String> signOut(
            HttpServletRequest request) {
        try {
            String accessToken = jwtService.extractTokenFromHeader(request);
            Claims claims = jwtService.extractClaimsCycle(accessToken);

            String email = claims.get("sub").toString();
            String owner = claims.get("iss").toString();

            authenticationService.signOut(email, owner);

            return ResponseEntity.ok("Signed out");
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @RequestParam("newPassword") String newPassword,
            HttpServletRequest request) {
        try {

            if (newPassword.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$")) {
                throw new CustomException("Invalid password. Must contain: " +
                        "uppercase letter, lowercase letter, number, special character. At least 8 characters long",
                        HttpStatus.BAD_REQUEST);
            }
            String code = request.getHeader("Register-key");
            String encoded = passwordEncoder.encode(newPassword);

            Claims claims = jwtService.extractClaimsCycle(code);
            String email = claims.get("sub").toString();
            String owner = claims.get("role").toString();
            String tokenType = claims.get("recognition").toString();

            authenticationService.checkForgotKey(
                    code,
                    ETokenRole.valueOf(tokenType),
                    ETokenRole.FORGOT_PASSWORD);

            System.out.println("after check");

            authenticationService.resetPassword(email, owner, encoded);

            return ResponseEntity.ok("Password has been changed");
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }

    }

}
