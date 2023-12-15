package com.example.auto_ria.controllers;

import com.example.auto_ria.dao.auth.RegisterKeyDaoSQL;
import com.example.auto_ria.dto.requests.RegisterRequestAdminDTO;
import com.example.auto_ria.dto.requests.RegisterRequestCustomerDTO;
import com.example.auto_ria.dto.requests.RegisterRequestManagerDTO;
import com.example.auto_ria.dto.requests.RegisterRequestSellerDTO;
import com.example.auto_ria.enums.ERole;
import com.example.auto_ria.enums.ETokenRole;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.models.requests.*;
import com.example.auto_ria.models.responses.auth.AuthenticationInfoResponse;
import com.example.auto_ria.models.responses.auth.AuthenticationResponse;
import com.example.auto_ria.models.user.AdministratorSQL;
import com.example.auto_ria.models.user.CustomerSQL;
import com.example.auto_ria.models.user.ManagerSQL;
import com.example.auto_ria.models.user.SellerSQL;
import com.example.auto_ria.services.auth.AuthenticationService;
import com.example.auto_ria.services.auth.JwtService;
import com.example.auto_ria.services.otherApi.CitiesService;
import com.example.auto_ria.services.user.AdministratorServiceMySQL;
import com.example.auto_ria.services.user.CustomersServiceMySQL;
import com.example.auto_ria.services.user.ManagerServiceMySQL;
import com.example.auto_ria.services.user.UsersServiceMySQLImpl;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
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
            @ModelAttribute @Valid RegisterRequestSellerDTO registerRequestDTO
    ) {
        try {

            citiesService.isValidUkrainianCity(registerRequestDTO.getRegion(), registerRequestDTO.getCity());

            if (usersServiceMySQL.isSellerByNumberPresent(registerRequestDTO.getNumber())) {
                throw new CustomException("User with this number already exists", HttpStatus.BAD_REQUEST);
            }

            if (usersServiceMySQL.isSellerByEmailPresent(registerRequestDTO.getEmail())) {
                throw new CustomException("User with this email already exists", HttpStatus.BAD_REQUEST);
            }

            String fileName = null;
            if (registerRequestDTO.getAvatar() != null) {
                fileName = registerRequestDTO.getAvatar().getOriginalFilename();
                usersServiceMySQL.transferAvatar(registerRequestDTO.getAvatar(), fileName);
            }

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
            String email = jwtService.extractUsername(code, ETokenRole.SELLER_ACTIVATE);
            return authenticationService.activateSeller(email, code);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PostMapping("/code-manager")
    public ResponseEntity<String> codeManager(
            @RequestParam("email") String email
    ) {
        try {
            Map<String, String> claims = new HashMap<>();
            claims.put("username", email);
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
            HttpServletRequest request
    ) {
        try {
            String code = request.getHeader("Register-key");

            if (code == null) {
                throw new CustomException("Register-key absent", HttpStatus.BAD_REQUEST);
            }

            if (managerServiceMySQL.isManagerByEmailPresent(registerRequestDTO.getEmail())) {
                throw new CustomException("User with this email already exists", HttpStatus.BAD_REQUEST);
            }

            System.out.println("code " + code);

            String key = authenticationService.checkRegistrationKey(
                    code,
                    registerRequestDTO.getEmail(),
                    ERole.MANAGER,
                    ETokenRole.MANAGER_REGISTER);

            String fileName = null;
            if (!registerRequestDTO.getAvatar().isEmpty()) {
                fileName = registerRequestDTO.getAvatar().getOriginalFilename();
                usersServiceMySQL.transferAvatar(registerRequestDTO.getAvatar(), fileName);
            }

            RegisterManagerRequest registerRequest = RegisterManagerRequest.builder()
                    .lastName(registerRequestDTO.getLastName())
                    .name(registerRequestDTO.getName())
                    .avatar(fileName)
                    .password(registerRequestDTO.getPassword())
                    .email(registerRequestDTO.getEmail())
                    .build();

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
            claims.put("username", email);
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
            HttpServletRequest request
    ) {
        try {
            String code = request.getHeader("Register-key");

            if (code == null) {
                throw new CustomException("Register-key absent", HttpStatus.BAD_REQUEST);
            }

            if (administratorServiceMySQL.isAdminByEmailPresent(registerRequestDTO.getEmail())) {
                throw new CustomException("User with this email already exists", HttpStatus.BAD_REQUEST);
            }

            String key = authenticationService.checkRegistrationKey(
                    code,
                    registerRequestDTO.getEmail(),
                    ERole.ADMIN,
                    ETokenRole.ADMIN_REGISTER);

            String fileName = null;
            System.out.println(registerRequestDTO + "register request");
                System.out.println("before avatar exists");
            if (registerRequestDTO.getAvatar() != null) {
                System.out.println("avatar exists");
                fileName = registerRequestDTO.getAvatar().getOriginalFilename();
                usersServiceMySQL.transferAvatar(registerRequestDTO.getAvatar(), fileName);
            }
                System.out.println("after avatar exists");

            RegisterAdminRequest registerRequest = RegisterAdminRequest.builder()
                    .lastName(registerRequestDTO.getLastName())
                    .name(registerRequestDTO.getName())
                    .avatar(fileName)
                    .password(registerRequestDTO.getPassword())
                    .email(registerRequestDTO.getEmail())
                    .build();

            return authenticationService.registerAdmin(registerRequest, key);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PostMapping("/register-customer")
    public ResponseEntity<String> registerCustomer(
            @ModelAttribute @Valid RegisterRequestCustomerDTO registerRequestDTO
    ) {
        try {

            if (usersServiceMySQL.isSellerByEmailPresent(registerRequestDTO.getEmail())) {
                throw new CustomException("User with this email already exists", HttpStatus.BAD_REQUEST);
            }

            String fileName = null;

            if (registerRequestDTO.getAvatar() != null) {
                fileName = registerRequestDTO.getAvatar().getOriginalFilename();
                usersServiceMySQL.transferAvatar(registerRequestDTO.getAvatar(), fileName);
            }

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

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> loginAll(@RequestBody LoginRequest loginRequest) {
        try {
            AuthenticationResponse authenticationResponse = AuthenticationResponse.builder().build();

            SellerSQL sellerSQL = usersServiceMySQL.getByEmail(loginRequest.getEmail());

            if (sellerSQL != null && !sellerSQL.getIsActivated().equals(true)) {
                throw new CustomException("Activate your account", HttpStatus.LOCKED);
            } else if (sellerSQL != null && sellerSQL.getIsActivated().equals(true)) {
                authenticationResponse = authenticationService.login(loginRequest);
            }

            CustomerSQL customerSQL = customersServiceMySQL.getByEmail(loginRequest.getEmail());

            if (customerSQL != null && !customerSQL.getIsActivated().equals(true)) {
                throw new CustomException("Activate your account", HttpStatus.LOCKED);
            } else if (customerSQL != null && customerSQL.getIsActivated().equals(true)) {
                authenticationResponse = authenticationService.loginCustomer(loginRequest);
            }

            ManagerSQL managerSQL = managerServiceMySQL.getByEmail(loginRequest.getEmail());

            if (managerSQL != null && !managerSQL.getIsActivated().equals(true)) {
                throw new CustomException("Activate your account", HttpStatus.LOCKED);
            } else if (managerSQL != null && managerSQL.getIsActivated().equals(true)) {
                authenticationResponse = authenticationService.loginManager(loginRequest);
            }

            AdministratorSQL administratorSQL = administratorServiceMySQL.getByEmail(loginRequest.getEmail());

            if (administratorSQL != null && !administratorSQL.getIsActivated().equals(true)) {
                throw new CustomException("Activate your account", HttpStatus.LOCKED);
            } else if (administratorSQL != null && administratorSQL.getIsActivated().equals(true)) {
                authenticationResponse = authenticationService.loginAdmin(loginRequest);
            }

            if (customerSQL == null && managerSQL == null && administratorSQL == null && sellerSQL == null) {
                throw new CustomException("Login or password is not valid", HttpStatus.FORBIDDEN);
            }

            return ResponseEntity.ok(authenticationResponse);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }

    }

    @PostMapping("/authenticate/admin")
    public ResponseEntity<AuthenticationResponse> loginAdmin(@RequestBody LoginRequest loginRequest) {
        try {
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
    public ResponseEntity<AuthenticationResponse> refresh(@RequestBody @Valid RefreshRequest refreshRequest) {
        try {
            return ResponseEntity.ok(authenticationService.refresh(refreshRequest));
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PostMapping("/refresh/manager")
    public ResponseEntity<AuthenticationResponse> refreshManager(@RequestBody @Valid RefreshRequest refreshRequest) {
        try {
            return ResponseEntity.ok(authenticationService.refreshManager(refreshRequest));
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PostMapping("/refresh/admin")
    public ResponseEntity<AuthenticationResponse> refreshAdmin(@RequestBody @Valid RefreshRequest refreshRequest) {
        try {
            return ResponseEntity.ok(authenticationService.refreshAdmin(refreshRequest));
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PostMapping("/refresh/customer")
    public ResponseEntity<AuthenticationResponse> refreshCustomer(@RequestBody @Valid RefreshRequest refreshRequest) {
        try {
            return ResponseEntity.ok(authenticationService.refreshCustomer(refreshRequest));
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refreshAll(@RequestBody @Valid RefreshRequest refreshRequest) {
        try {
            return ResponseEntity.ok(authenticationService.refreshAll(refreshRequest));
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PostMapping("/change-passwords")
    public ResponseEntity<AuthenticationInfoResponse> changePassword(
            @RequestParam("newPassword") String newPassword,
            HttpServletRequest request) {
        try {
            System.out.println("reset");
            if (!newPassword.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$")) {
                throw new CustomException("Invalid password. Must contain: " +
                        "uppercase letter, lowercase letter, number, special character. At least 8 characters long",
                        HttpStatus.BAD_REQUEST);
            }
            System.out.println("reset1");
            String encoded = passwordEncoder.encode(newPassword);

            String accessToken = jwtService.extractTokenFromHeader(request);
            Claims claims = jwtService.extractClaimsCycle(accessToken);

            String email = claims.get("sub").toString();
            String owner = claims.get("iss").toString();

            return ResponseEntity.ok(authenticationService.resetPassword(email, owner, encoded));
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(
            @RequestParam("email") String email) {
        try {
            System.out.println("465");
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

            System.out.println(claims + " claims sign out");
            String email = claims.get("sub").toString();
            String owner = claims.get("iss").toString();

            authenticationService.signOut(email, owner);

            return ResponseEntity.ok("Signed out");
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<AuthenticationInfoResponse> resetPassword(
            @RequestParam("newPassword") String newPassword,
            HttpServletRequest request) {
        try {
            System.out.println(500);
            String code = request.getHeader("Register-key");
            System.out.println(newPassword + "new pass");

            if (newPassword.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$")) {
                throw new CustomException("Invalid password. Must contain: " +
                        "uppercase letter, lowercase letter, number, special character. At least 8 characters long",
                        HttpStatus.BAD_REQUEST);
            }
            String encoded = passwordEncoder.encode(newPassword);
            System.out.println(encoded + "encoded");


            Claims claims = jwtService.extractClaimsCycle(code);
            String email = claims.get("sub").toString();
            String owner = claims.get("role").toString();
            String tokenType = claims.get("recognition").toString();

            authenticationService.checkForgotKey(
                    code,
                    ETokenRole.valueOf(tokenType),
                    ETokenRole.FORGOT_PASSWORD);

            return ResponseEntity.ok(authenticationService.resetPassword(email, owner, encoded));
        } catch (CustomException e) {
            System.out.println(e.getMessage());
            throw new CustomException(e.getMessage(), e.getStatus());
        }

    }

}
