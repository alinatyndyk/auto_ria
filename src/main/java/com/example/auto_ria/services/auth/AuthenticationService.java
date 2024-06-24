package com.example.auto_ria.services.auth;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.auto_ria.configurations.providers.UserAuthenticationProvider;
import com.example.auto_ria.dao.auth.RegisterKeyDaoSQL;
import com.example.auto_ria.dao.auth.UserAuthDaoSQL;
import com.example.auto_ria.dao.user.UserDaoSQL;
import com.example.auto_ria.enums.EMail;
import com.example.auto_ria.enums.ERole;
import com.example.auto_ria.enums.ETokenRole;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.mail.FMService;
import com.example.auto_ria.models.auth.AuthSQL;
import com.example.auto_ria.models.auth.RegisterKey;
import com.example.auto_ria.models.requests.LoginRequest;
import com.example.auto_ria.models.requests.RefreshRequest;
import com.example.auto_ria.models.requests.RegisterUserRequest;
import com.example.auto_ria.models.responses.auth.AuthenticationResponse;
import com.example.auto_ria.models.responses.user.UserResponse;
import com.example.auto_ria.models.user.UserSQL;
import com.example.auto_ria.services.CommonService;
import com.example.auto_ria.services.user.UsersServiceMySQLImpl;

import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AuthenticationService {

    private JwtService jwtService;
    private UserDaoSQL userDaoSQL;
    private UsersServiceMySQLImpl usersServiceMySQL;
    private UserAuthDaoSQL userAuthDaoSQL;
    private UserAuthenticationProvider sellerAuthenticationManager;
    private RegisterKeyDaoSQL registerKeyDaoSQL;
    private PasswordEncoder passwordEncoder;
    private CommonService commonService;
    private FMService mailer;

    // todo auth pre
    public UserResponse getByToken(String token) {
        AuthSQL authSQL = userAuthDaoSQL.findByAccessToken(token);
        if (authSQL == null) {
            throw new CustomException("Invalid access token", HttpStatus.BAD_REQUEST);
        }
        return commonService.createUserResponse(usersServiceMySQL.getById(authSQL.getPersonId()));

    }

    public ResponseEntity<String> registerUser(RegisterUserRequest registerRequest) {
        try {

            UserSQL user = UserSQL.userBuilder()
                    .name(registerRequest.getName())
                    .lastName(registerRequest.getLastName())
                    .email(registerRequest.getEmail())
                    .password(passwordEncoder.encode(registerRequest.getPassword()))
                    .roles(List.of(ERole.USER))
                    .avatar(registerRequest.getAvatar())
                    .city(registerRequest.getCity())
                    .region(registerRequest.getRegion())
                    .number(registerRequest.getNumber())
                    .build();

            user.setIsActivated(false);
            userDaoSQL.save(user);

            String activateToken = jwtService.generateRegisterKey(
                    user.getEmail(), ETokenRole.USER_ACTIVATE);

            registerKeyDaoSQL.save(RegisterKey.builder().registerKey(activateToken).build());

            HashMap<String, Object> variables = new HashMap<>();
            variables.put("name", registerRequest.getName());
            variables.put("role", ETokenRole.USER);
            variables.put("code", activateToken);

            try {
                mailer.sendEmail(registerRequest.getEmail(), EMail.REGISTER_KEY, variables);
            } catch (Exception e) {
                throw new CustomException("Something went wrong... Try again later", HttpStatus.CONFLICT);
            }

            return ResponseEntity.ok("Check your email for activation");
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        } catch (Exception e) {
            throw new CustomException("Register failed", HttpStatus.EXPECTATION_FAILED);
        }
    }

    public ResponseEntity<AuthenticationResponse> registerUserWithAuthority(RegisterUserRequest registerRequest,
            String key,
            ERole role) {
        try {

            UserSQL user = UserSQL.userBuilder()
                    .name(registerRequest.getName())
                    .lastName(registerRequest.getLastName())
                    .email(registerRequest.getEmail())
                    .password(passwordEncoder.encode(registerRequest.getPassword()))
                    .roles(List.of(role))
                    .avatar(registerRequest.getAvatar())
                    .city(registerRequest.getCity())
                    .region(registerRequest.getRegion())
                    .number(registerRequest.getNumber())
                    .build();

            user.setIsActivated(true);
            userDaoSQL.save(user);

            AuthenticationResponse authenticationResponse = null;

            if (role.equals(ERole.MANAGER)) {
                authenticationResponse = jwtService.generateManagerTokenPair(user);
                userAuthDaoSQL.save(AuthSQL.builder().role(ERole.MANAGER).personId(user.getId())
                        .accessToken(authenticationResponse.getAccessToken())
                        .refreshToken(authenticationResponse.getRefreshToken()).build());
            } else if (role.equals(ERole.ADMIN)) {
                authenticationResponse = jwtService.generateAdminTokenPair(user);
                userAuthDaoSQL.save(AuthSQL.builder().role(ERole.ADMIN).personId(user.getId())
                        .accessToken(authenticationResponse.getAccessToken())
                        .refreshToken(authenticationResponse.getRefreshToken()).build());
            } else {
                throw new CustomException("Unknown role", HttpStatus.CONFLICT);
            }

            Map<String, Object> args = new HashMap<>();
            args.put("name", user.getName() + user.getLastName());
            mailer.sendEmail(user.getEmail(), EMail.WELCOME, args);

            registerKeyDaoSQL.delete(registerKeyDaoSQL.findByRegisterKey(key));

            return ResponseEntity.ok(authenticationResponse);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        } catch (Exception e) {
            throw new CustomException("Register failed", HttpStatus.EXPECTATION_FAILED);
        }
    }

    public ResponseEntity<AuthenticationResponse> activateUser(String email, String code) {
        try {
            UserSQL userSQL = userDaoSQL.findUserByEmail(email);

            if (userSQL.getIsActivated()) {
                throw new CustomException("User is already activated", HttpStatus.BAD_REQUEST);
            }

            userSQL.setIsActivated(true);

            AuthenticationResponse tokenPair = jwtService.generateUserTokenPair(userSQL);

            userDaoSQL.save(userSQL);

            userAuthDaoSQL.save(AuthSQL.builder().role(ERole.USER)
                    .personId(userSQL.getId()).accessToken(tokenPair.getAccessToken())
                    .refreshToken(tokenPair.getRefreshToken()).build());

            registerKeyDaoSQL.delete(registerKeyDaoSQL.findByRegisterKey(code));

            Map<String, Object> vars = new HashMap<>();
            vars.put("name", userSQL.getName());

            mailer.sendEmail(userSQL.getEmail(), EMail.WELCOME, vars);

            return ResponseEntity.ok(AuthenticationResponse.builder()
                    .accessToken(tokenPair.getAccessToken())
                    .refreshToken(tokenPair.getRefreshToken())
                    .build());
        } catch (Exception e) {
            throw new CustomException("Failed activation", HttpStatus.EXPECTATION_FAILED);
        }

    }

    public ResponseEntity<String> codeManager(String email, String code) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("code", code);
            map.put("role", ERole.MANAGER.name());

            mailer.sendEmail(email, EMail.REGISTER, map);

            registerKeyDaoSQL.save(RegisterKey.builder().registerKey(code).build());

            return ResponseEntity.ok("Email sent");
        } catch (Exception e) {
            throw new CustomException("Error creating manager code", HttpStatus.EXPECTATION_FAILED);
        }
    }

    public ResponseEntity<String> codeAdmin(String email, String code) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("code", code);
            map.put("role", ERole.ADMIN.name());

            mailer.sendEmail(email, EMail.REGISTER, map);

            registerKeyDaoSQL.save(RegisterKey.builder().registerKey(code).build());

            return ResponseEntity.ok("Email sent");
        } catch (Exception e) {
            throw new CustomException("Error creating admin code: " + e.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }
    }

    public AuthenticationResponse login(LoginRequest loginRequest) {
        try {
            UserSQL user = userDaoSQL.findUserByEmail(loginRequest.getEmail());

            if (user == null) {
                throw new CustomException("User not found", HttpStatus.NOT_FOUND);
            }

            sellerAuthenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            user.getEmail(),
                            loginRequest.getPassword(),
                            user.getAuthorities()));

            if (!user.getIsActivated()) {
                throw new CustomException("Activate your account to access secured endpoints", HttpStatus.FORBIDDEN);
            }

            AuthenticationResponse tokenPair = jwtService.generateUserTokenPair(user);

            userAuthDaoSQL.save(AuthSQL.builder().role(ERole.USER).personId(user.getId())
                    .accessToken(tokenPair.getAccessToken())
                    .refreshToken(tokenPair.getRefreshToken()).build());

            userDaoSQL.save(user);

            return tokenPair;
        } catch (CustomException e) {
            throw new CustomException("Failed login: " + e.getMessage(), e.getStatus());
        } catch (Exception e) {
            throw new CustomException("Failed login: " + e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    public AuthenticationResponse refreshAll(RefreshRequest refreshRequest) {
        try {
            String refreshToken = refreshRequest.getRefreshToken();

            if (jwtService.isTokenExprired(refreshToken)) {
                throw new CustomException("Token expired", HttpStatus.FORBIDDEN);
            }

            String username;
            AuthenticationResponse tokenPair = null;

            if ((username = jwtService.extractUsername(refreshToken, ETokenRole.USER)) != null &&
                    userAuthDaoSQL.findByRefreshToken(refreshToken) != null) {
                UserSQL userSQL = userDaoSQL.findUserByEmail(username);
                if (userSQL != null) {
                    tokenPair = jwtService.generateUserTokenPair(userSQL);

                    userAuthDaoSQL.deleteAllByRefreshToken(refreshToken);
                    userAuthDaoSQL.save(AuthSQL.builder().role(ERole.USER).personId(userSQL.getId())
                            .accessToken(tokenPair.getAccessToken())
                            .refreshToken(tokenPair.getRefreshToken()).build());
                }
            }

            if (username == null) {
                throw new CustomException("Token invalid", HttpStatus.FORBIDDEN);
            }

            return AuthenticationResponse.builder()
                    .accessToken(Objects.requireNonNull(tokenPair).getAccessToken())
                    .refreshToken(tokenPair.getRefreshToken()).build();

        } catch (CustomException e) {
            throw new CustomException("Failed refresh: " + e.getMessage(), e.getStatus());
        } catch (Exception e) {
            throw new CustomException("Failed refresh: " + e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    public void forgotPassword(String email) {
        try {

            if (usersServiceMySQL.getByEmail(email) == null) {
                throw new CustomException("User not found", HttpStatus.BAD_REQUEST);
            }

            String code = jwtService.generateRegisterKey(email, ETokenRole.FORGOT_PASSWORD);
            registerKeyDaoSQL.save(RegisterKey.builder().registerKey(code).build());

            Map<String, Object> args = new HashMap<>();
            args.put("email", email);
            args.put("time", LocalDateTime.now());
            args.put("code", code);
            mailer.sendEmail(email, EMail.FORGOT_PASSWORD, args);

        } catch (Exception e) {
            throw new CustomException("Forgot password error" + e.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }
    }

    public AuthenticationResponse resetPassword(String email, String encoded) {
        AuthenticationResponse authenticationResponse;

        try {

            UserSQL userSQL = usersServiceMySQL.getByEmail(email);
            userSQL.setPassword(encoded);
            userDaoSQL.save(userSQL);
            userAuthDaoSQL.deleteAllByPersonId(userSQL.getId());

            AuthenticationResponse authentication = jwtService.generateUserTokenPair(userSQL);

            userAuthDaoSQL.save(AuthSQL.builder().role(ERole.USER)
                    .accessToken(authentication.getAccessToken())
                    .refreshToken(authentication.getRefreshToken())
                    .id(userSQL.getId())
                    .build());

            authenticationResponse = authentication;

        } catch (CustomException e) {
            throw new CustomException("Failed reset: " + e.getMessage(), e.getStatus());
        } catch (Exception e) {
            throw new CustomException("Failed reset: " + e.getMessage(), HttpStatus.CONFLICT);
        }
        return authenticationResponse; // from auth info res/ to auth res
    }

    public void signOut(String email) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.getName().equals(email)) {
                SecurityContextHolder.clearContext();
            }
            UserSQL userSQL = usersServiceMySQL.getByEmail(email);
            userAuthDaoSQL.deleteAllByPersonId(userSQL.getId());

        } catch (CustomException e) {
            throw new CustomException("Failed sign out: " + e.getMessage(), e.getStatus());
        } catch (Exception e) {
            throw new CustomException("Failed sign out: " + e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    private void checkKey(String authorizationHeader,
            ETokenRole tokenRecognition,
            ETokenRole funcRecognition) {

        if (authorizationHeader == null) {
            throw new CustomException("Register key required", HttpStatus.FORBIDDEN);
        }

        RegisterKey registerKey = registerKeyDaoSQL.findByRegisterKey(authorizationHeader);

        if (registerKey == null) {
            throw new CustomException("Key is not valid", HttpStatus.FORBIDDEN);
        }

        if (!funcRecognition.equals(tokenRecognition)) {
            throw new CustomException("Invalid key recognition", HttpStatus.FORBIDDEN);
        }

        if (jwtService.isTokenExprired(authorizationHeader)) {
            throw new CustomException("Key expired", HttpStatus.FORBIDDEN);
        }
    }

    public String checkRegistrationKey(String authorizationHeader,
            String email,
            ERole role,
            ETokenRole funcRecognition) {

        Claims claims = jwtService.extractClaimsCycle(authorizationHeader);
        String tokenRec = claims.getIssuer();

        checkKey(authorizationHeader, ETokenRole.valueOf(tokenRec), funcRecognition);

        if (!tokenRec.equals(funcRecognition.name())) {
            throw new CustomException("The key is not valid for creation of " + role.name().toLowerCase(),
                    HttpStatus.FORBIDDEN);
        }

        if (!jwtService.isKeyValid(authorizationHeader, email, ETokenRole.valueOf(funcRecognition.name()))) {
            throw new CustomException("Not valid key owner", HttpStatus.FORBIDDEN);
        }

        return authorizationHeader;
    }

    public void checkForgotKey(String authorizationHeader,
            ETokenRole tokenRecognition,
            ETokenRole funcRecognition) {

        checkKey(authorizationHeader, tokenRecognition, funcRecognition);
    }
}
