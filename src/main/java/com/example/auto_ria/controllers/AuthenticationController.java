package com.example.auto_ria.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.auto_ria.dto.requests.RegisterRequestAdminDTO;
import com.example.auto_ria.dto.requests.RegisterRequestManagerDTO;
import com.example.auto_ria.dto.requests.RegisterRequestUserDTO;
import com.example.auto_ria.enums.ERole;
import com.example.auto_ria.enums.ETokenRole;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.models.requests.LoginRequest;
import com.example.auto_ria.models.requests.RefreshRequest;
import com.example.auto_ria.models.requests.RegisterAdminRequest;
import com.example.auto_ria.models.requests.RegisterManagerRequest;
import com.example.auto_ria.models.requests.RegisterUserRequest;
import com.example.auto_ria.models.responses.auth.AuthenticationResponse;
import com.example.auto_ria.models.user.AdministratorSQL;
import com.example.auto_ria.models.user.ManagerSQL;
import com.example.auto_ria.models.user.UserSQL;
import com.example.auto_ria.services.auth.AuthenticationService;
import com.example.auto_ria.services.auth.JwtService;
import com.example.auto_ria.services.otherApi.CitiesService;
import com.example.auto_ria.services.user.AdministratorServiceMySQL;
import com.example.auto_ria.services.user.ManagerServiceMySQL;
import com.example.auto_ria.services.user.UsersServiceMySQLImpl;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthenticationController {

    private AuthenticationService authenticationService;

    private UsersServiceMySQLImpl usersServiceMySQL;
    private AdministratorServiceMySQL administratorServiceMySQL;
    private ManagerServiceMySQL managerServiceMySQL;

    private JwtService jwtService;
    private PasswordEncoder passwordEncoder;
    private CitiesService citiesService;

    // ------------------------------ mix seller/customer

    @PostMapping("/register-user")
    public ResponseEntity<String> registerUser(
            @ModelAttribute @Valid RegisterRequestUserDTO registerRequestDTO) {
        try {

            citiesService.isValidUkrainianCity(registerRequestDTO.getRegion(),
                    registerRequestDTO.getCity());

            if (usersServiceMySQL.isUserByNumberPresent(registerRequestDTO.getNumber())) {
                throw new CustomException("User with this number already exists",
                        HttpStatus.BAD_REQUEST);
            }

            if (usersServiceMySQL.isUserByEmailPresent(registerRequestDTO.getEmail())) {
                throw new CustomException("User with this email already exists",
                        HttpStatus.BAD_REQUEST);
            }

            String fileName = null;
            if (registerRequestDTO.getAvatar() != null) {
                fileName = registerRequestDTO.getAvatar().getOriginalFilename();
                usersServiceMySQL.transferAvatar(registerRequestDTO.getAvatar(), fileName);
            }

            RegisterUserRequest registerRequest = new RegisterUserRequest(
                    registerRequestDTO.getCity(),
                    registerRequestDTO.getRegion(),
                    registerRequestDTO.getName(),
                    registerRequestDTO.getLastName(),
                    registerRequestDTO.getEmail(),
                    registerRequestDTO.getNumber(),
                    fileName,
                    registerRequestDTO.getPassword());
            return authenticationService.registerUser(registerRequest);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    // ------------------------------
    // mix
    @PostMapping("/activate-user")
    public ResponseEntity<AuthenticationResponse> activateSeller(
            @RequestParam("code") String code) {
        try {
            if (jwtService.isTokenExprired(code)) {
                throw new CustomException("Activation key expired. Your account has been deleted",
                        HttpStatus.FORBIDDEN);
            }
            String email = jwtService.extractUsername(code, ETokenRole.USER_ACTIVATE);
            return authenticationService.activateUser(email, code);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }
    // ----------------------------------

    @PostMapping("/code-manager")
    public ResponseEntity<String> codeManager(
            @RequestParam("email") String email) {
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
            HttpServletRequest request) {
        try {
            String code = request.getHeader("Register-key");

            if (code == null) {
                throw new CustomException("Register-key absent", HttpStatus.BAD_REQUEST);
            }

            if (managerServiceMySQL.isManagerByEmailPresent(registerRequestDTO.getEmail())) {
                throw new CustomException("User with this email already exists", HttpStatus.BAD_REQUEST);
            }

            String key = authenticationService.checkRegistrationKey(
                    code,
                    registerRequestDTO.getEmail(),
                    ERole.MANAGER,
                    ETokenRole.MANAGER_REGISTER);

            String fileName = null;

            if (registerRequestDTO.getAvatar() != null) {
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
            @RequestParam("email") String email) {
        try {
            System.out.println("heyyyyyyyyyyyyyyyyyyyyyyyyyyyyy");
            Map<String, String> claims = new HashMap<>();
            claims.put("username", email);
            claims.put("role", ETokenRole.ADMIN_REGISTER.name());

            String code = jwtService.generateRegistrationCode(claims, email, ETokenRole.ADMIN_REGISTER);
            System.out.println(187);
            return authenticationService.codeAdmin(email, code);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PostMapping("/register-admin")
    public ResponseEntity<AuthenticationResponse> registerAdmin(
            @ModelAttribute @Valid RegisterRequestAdminDTO registerRequestDTO,
            HttpServletRequest request) {
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
            if (registerRequestDTO.getAvatar() != null) {
                fileName = registerRequestDTO.getAvatar().getOriginalFilename();
                usersServiceMySQL.transferAvatar(registerRequestDTO.getAvatar(), fileName);
            }

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

    // @PostMapping("/register-customer")
    // public ResponseEntity<String> registerCustomer(
    // @ModelAttribute @Valid RegisterRequestCustomerDTO registerRequestDTO) {
    // try {

    // if (usersServiceMySQL.isSellerByEmailPresent(registerRequestDTO.getEmail()))
    // {
    // throw new CustomException("User with this email already exists",
    // HttpStatus.BAD_REQUEST);
    // }

    // String fileName = null;

    // if (registerRequestDTO.getAvatar() != null) {
    // fileName = registerRequestDTO.getAvatar().getOriginalFilename();
    // usersServiceMySQL.transferAvatar(registerRequestDTO.getAvatar(), fileName);
    // }

    // RegisterCustomerRequest registerRequest = new RegisterCustomerRequest(
    // registerRequestDTO.getCity(),
    // registerRequestDTO.getRegion(),
    // registerRequestDTO.getName(),
    // registerRequestDTO.getLastName(),
    // registerRequestDTO.getEmail(),
    // fileName,
    // registerRequestDTO.getPassword());
    // return authenticationService.registerCustomer(registerRequest);
    // } catch (CustomException e) {
    // throw new CustomException(e.getMessage(), e.getStatus());
    // }
    // }

    // @PostMapping("/activate-customer-account")
    // public ResponseEntity<AuthenticationResponse> activateCustomer(
    // @RequestParam("code") String code) {
    // try {
    // if (jwtService.isTokenExprired(code)) {
    // throw new CustomException("Activation key expired. Your account has been
    // deleted",
    // HttpStatus.FORBIDDEN);
    // }
    // String email = jwtService.extractUsername(code,
    // ETokenRole.CUSTOMER_ACTIVATE);
    // return authenticationService.activateCustomer(email, code);
    // } catch (CustomException e) {
    // throw new CustomException(e.getMessage(), e.getStatus());
    // }
    // }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> loginAll(@RequestBody LoginRequest loginRequest) {
        try {

            System.out.println("login request*********" + loginRequest);
            AuthenticationResponse authenticationResponse = AuthenticationResponse.builder().build();

            UserSQL userSQL = usersServiceMySQL.getByEmail(loginRequest.getEmail());

            if (userSQL != null && !userSQL.getIsActivated().equals(true)) {// ? equals !true -> false then?
                throw new CustomException("Activate your account", HttpStatus.LOCKED);
            } else if (userSQL != null && userSQL.getIsActivated().equals(true)) {
                authenticationResponse = authenticationService.login(loginRequest);
            }

            ManagerSQL managerSQL = managerServiceMySQL.getByEmail(loginRequest.getEmail());

            if (managerSQL != null && !managerSQL.getIsActivated().equals(true)) {
                throw new CustomException("Activate your account", HttpStatus.LOCKED);
            } else if (managerSQL != null && managerSQL.getIsActivated().equals(true)) {
                authenticationResponse = authenticationService.loginManager(loginRequest);
            }

            AdministratorSQL administratorSQL = administratorServiceMySQL.getByEmail(loginRequest.getEmail());
            System.out.println("++++jjjadmin************" + administratorSQL);

            if (administratorSQL != null && !administratorSQL.getIsActivated().equals(true)) {
                throw new CustomException("Activate your account", HttpStatus.LOCKED);
            } else if (administratorSQL != null && administratorSQL.getIsActivated().equals(true)) {
                authenticationResponse = authenticationService.loginAdmin(loginRequest);
                System.out.println("authenticationResponse" + authenticationResponse);
            }

            if (userSQL == null && managerSQL == null && administratorSQL == null) {
                System.out.println("yes");
                throw new CustomException("Login or password is not valid. Try again", HttpStatus.FORBIDDEN);
            }

            return ResponseEntity.ok(authenticationResponse);
        } catch (CustomException e) {
            System.out.println("yes here 2");
            throw new CustomException(e.getMessage(), e.getStatus());
        }

    }

    // @PostMapping("/refresh/seller")
    // public ResponseEntity<AuthenticationResponse> refresh(@RequestBody @Valid
    // RefreshRequest refreshRequest) {
    // try {
    // return ResponseEntity.ok(authenticationService.refresh(refreshRequest));
    // } catch (CustomException e) {
    // throw new CustomException(e.getMessage(), e.getStatus());
    // }
    // }

    // @PostMapping("/refresh/manager")
    // public ResponseEntity<AuthenticationResponse> refreshManager(@RequestBody
    // @Valid RefreshRequest refreshRequest) {
    // try {
    // return
    // ResponseEntity.ok(authenticationService.refreshManager(refreshRequest));
    // } catch (CustomException e) {
    // throw new CustomException(e.getMessage(), e.getStatus());
    // }
    // }

    // @PostMapping("/refresh/admin")
    // public ResponseEntity<AuthenticationResponse> refreshAdmin(@RequestBody
    // @Valid RefreshRequest refreshRequest) {
    // try {
    // return ResponseEntity.ok(authenticationService.refreshAdmin(refreshRequest));
    // } catch (CustomException e) {
    // throw new CustomException(e.getMessage(), e.getStatus());
    // }
    // }

    // @PostMapping("/refresh/customer")
    // public ResponseEntity<AuthenticationResponse> refreshCustomer(@RequestBody
    // @Valid RefreshRequest refreshRequest) {
    // try {
    // return
    // ResponseEntity.ok(authenticationService.refreshCustomer(refreshRequest));
    // } catch (CustomException e) {
    // throw new CustomException(e.getMessage(), e.getStatus());
    // }
    // }

    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refreshAll(@RequestBody @Valid RefreshRequest refreshRequest) {
        try {
            return ResponseEntity.ok(authenticationService.refreshAll(refreshRequest));
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PostMapping("/change-passwords")
    public ResponseEntity<AuthenticationResponse> changePassword(
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

            return ResponseEntity.ok(authenticationService.resetPassword(email, owner, encoded));
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
    public ResponseEntity<AuthenticationResponse> resetPassword(
            @RequestParam("newPassword") String newPassword,
            HttpServletRequest request) {
        try {
            String code = request.getHeader("Register-key");

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
