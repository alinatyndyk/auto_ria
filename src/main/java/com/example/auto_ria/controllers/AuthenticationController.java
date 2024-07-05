package com.example.auto_ria.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.auto_ria.dto.requests.RegisterRequestUserDTO;
import com.example.auto_ria.enums.ERole;
import com.example.auto_ria.enums.ETokenRole;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.models.requests.LoginRequest;
import com.example.auto_ria.models.requests.RefreshRequest;
import com.example.auto_ria.models.requests.RegisterUserRequest;
import com.example.auto_ria.models.responses.auth.AuthenticationResponse;
import com.example.auto_ria.models.user.UserSQL;
import com.example.auto_ria.services.auth.AuthenticationService;
import com.example.auto_ria.services.auth.JwtService;
import com.example.auto_ria.services.otherApi.CitiesService;
import com.example.auto_ria.services.user.UsersServiceMySQLImpl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
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

    private JwtService jwtService;
    private PasswordEncoder passwordEncoder;
    private CitiesService citiesService;

    @PostMapping("/register-user")
    public ResponseEntity<String> registerUser(
            @ModelAttribute @Valid RegisterRequestUserDTO registerRequestDTO, HttpServletRequest request) {
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

    @PostMapping("/register-user/with-authority")
    public ResponseEntity<AuthenticationResponse> registerUserWithAuth(
            @ModelAttribute @Valid RegisterRequestUserDTO registerRequestDTO, HttpServletRequest request) {
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

            String key = registerRequestDTO.getCode();

            if (key != null) {
                Claims claims = jwtService.extractClaimsCycle(key);
                if (claims.getIssuer().equals(ETokenRole.MANAGER_REGISTER.name())) {
                    authenticationService.checkRegistrationKey(
                            key,
                            registerRequestDTO.getEmail(),
                            ERole.MANAGER,
                            ETokenRole.valueOf(claims.getIssuer()));

                    return authenticationService.registerUserWithAuthority(registerRequest,
                            registerRequestDTO.getCode(), ERole.MANAGER);
                } else if (claims.getIssuer().equals(ETokenRole.ADMIN_REGISTER.name())) {
                    authenticationService.checkRegistrationKey(
                            key,
                            registerRequestDTO.getEmail(),
                            ERole.ADMIN,
                            ETokenRole.valueOf(claims.getIssuer()));
                    return authenticationService.registerUserWithAuthority(registerRequest,
                            registerRequestDTO.getCode(), ERole.ADMIN);
                } else {
                    throw new CustomException("Invalid token for user creation",
                            HttpStatus.BAD_REQUEST);
                }
            } else {
                throw new CustomException("Auth key absent",
                        HttpStatus.BAD_REQUEST);
            }

        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PostMapping("/activate-user")
    public ResponseEntity<AuthenticationResponse> activateSeller(
            @RequestParam("code") String code) {
        try {

            jwtService.isTokenExprired(code);

            String email = jwtService.extractUsername(code, ETokenRole.USER_ACTIVATE);
            return authenticationService.activateUser(email, code);
        } catch (ExpiredJwtException e) {
            throw new CustomException("Activation key expired. Your account has been deleted",
                    HttpStatus.FORBIDDEN);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/code-manager")
    public ResponseEntity<String> codeManager(
            @RequestParam("email") String email) {
        try {
            Map<String, String> claims = new HashMap<>();
            claims.put("username", email);
            claims.put("role", ETokenRole.MANAGER_REGISTER.name());

            String code = jwtService.generateRegistrationCode(claims, email, ETokenRole.MANAGER_REGISTER);

            if (!usersServiceMySQL.getByEmail(email).equals(null)) {
                throw new CustomException("User with this email already exists. Do you want to change their role?",
                        HttpStatus.BAD_REQUEST);
            }

            return authenticationService.codeManager(email, code);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PostMapping("/register-manager")
    public ResponseEntity<AuthenticationResponse> registerManager(
            @ModelAttribute @Valid RegisterRequestUserDTO registerRequestDTO,
            HttpServletRequest request) {
        try {
            String code = request.getHeader("Register-key");

            if (code == null) {
                throw new CustomException("Register-key absent", HttpStatus.BAD_REQUEST);
            }

            if (usersServiceMySQL.isUserByEmailPresent(registerRequestDTO.getEmail())) {
                throw new CustomException("User with this email already exists",
                        HttpStatus.BAD_REQUEST);
            }

            if (usersServiceMySQL.isUserByNumberPresent(registerRequestDTO.getEmail())) {
                throw new CustomException("User with this number already exists",
                        HttpStatus.BAD_REQUEST);
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

            RegisterUserRequest registerRequest = new RegisterUserRequest(
                    registerRequestDTO.getCity(),
                    registerRequestDTO.getRegion(),
                    registerRequestDTO.getName(),
                    registerRequestDTO.getLastName(),
                    registerRequestDTO.getEmail(),
                    registerRequestDTO.getNumber(),
                    fileName,
                    registerRequestDTO.getPassword());

            return authenticationService.registerUserWithAuthority(registerRequest, key, ERole.MANAGER);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/code-admin")
    public ResponseEntity<String> codeAdmin(
            @RequestParam("email") String email) {
        try {
            Map<String, String> claims = new HashMap<>();
            claims.put("username", email);
            claims.put("role", ETokenRole.ADMIN_REGISTER.name());

            String code = jwtService.generateRegistrationCode(claims, email,
                    ETokenRole.ADMIN_REGISTER);

            if (!usersServiceMySQL.getByEmail(email).equals(null)) {
                throw new CustomException("User with this email already exists. Do you want to change their role?",
                        HttpStatus.BAD_REQUEST);
            }

            return authenticationService.codeAdmin(email, code);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PostMapping("/register-admin")
    public ResponseEntity<AuthenticationResponse> registerAdmin(
            @ModelAttribute @Valid RegisterRequestUserDTO registerRequestDTO,
            HttpServletRequest request) {
        try {
            String code = request.getHeader("Register-key");

            if (code == null) {
                throw new CustomException("Register-key absent", HttpStatus.BAD_REQUEST);
            }

            if (usersServiceMySQL.isUserByEmailPresent(registerRequestDTO.getEmail())) {
                throw new CustomException("User with this email already exists",
                        HttpStatus.BAD_REQUEST);
            }

            if (usersServiceMySQL.isUserByNumberPresent(registerRequestDTO.getNumber())) {
                throw new CustomException("User with this number already exists",
                        HttpStatus.BAD_REQUEST);
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

            RegisterUserRequest registerRequest = new RegisterUserRequest(
                    registerRequestDTO.getCity(),
                    registerRequestDTO.getRegion(),
                    registerRequestDTO.getName(),
                    registerRequestDTO.getLastName(),
                    registerRequestDTO.getEmail(),
                    registerRequestDTO.getNumber(),
                    fileName,
                    registerRequestDTO.getPassword());

            return authenticationService.registerUserWithAuthority(registerRequest, key, ERole.ADMIN);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> loginAll(@RequestBody LoginRequest loginRequest) {
        try {
            AuthenticationResponse authenticationResponse = AuthenticationResponse.builder().build();

            UserSQL userSQL = usersServiceMySQL.getByEmail(loginRequest.getEmail());

            if (userSQL != null && !userSQL.getIsActivated().equals(true)) {
                throw new CustomException("Activate your account", HttpStatus.LOCKED);
            } else if (userSQL != null && userSQL.getIsActivated().equals(true)) {
                authenticationResponse = authenticationService.login(loginRequest);
            }

            if (userSQL == null) {
                throw new CustomException("Login or password is not valid. Try again", HttpStatus.FORBIDDEN);
            }

            return ResponseEntity.ok(authenticationResponse);
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

    @PreAuthorize("hasRole('ADMIN', 'MANAGER', 'USER')")
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

            return ResponseEntity.ok(authenticationService.resetPassword(email, encoded));
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

    @PreAuthorize("hasRole('ADMIN', 'MANAGER', 'USER')")
    @PostMapping("/sign-out")
    public ResponseEntity<String> signOut(
            HttpServletRequest request) {
        try {
            String accessToken = jwtService.extractTokenFromHeader(request);
            Claims claims = jwtService.extractClaimsCycle(accessToken);

            String email = claims.get("sub").toString();

            authenticationService.signOut(email);

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

            Claims claims = jwtService.extractClaimsCycle(code);
            String email = claims.get("sub").toString();
            String tokenType = claims.get("recognition").toString();

            authenticationService.checkForgotKey(
                    code,
                    ETokenRole.valueOf(tokenType),
                    ETokenRole.FORGOT_PASSWORD);

            return ResponseEntity.ok(authenticationService.resetPassword(email, encoded));
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }

    }

}
