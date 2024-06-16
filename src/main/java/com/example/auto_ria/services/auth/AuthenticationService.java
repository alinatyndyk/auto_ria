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

import com.example.auto_ria.configurations.providers.AdminAuthenticationProvider;
import com.example.auto_ria.configurations.providers.ManagerAuthenticationProvider;
import com.example.auto_ria.configurations.providers.UserAuthenticationProvider;
import com.example.auto_ria.dao.auth.AdminAuthDaoSQL;
import com.example.auto_ria.dao.auth.ManagerAuthDaoSQL;
import com.example.auto_ria.dao.auth.RegisterKeyDaoSQL;
import com.example.auto_ria.dao.auth.UserAuthDaoSQL;
import com.example.auto_ria.dao.user.AdministratorDaoSQL;
import com.example.auto_ria.dao.user.ManagerDaoSQL;
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
import com.example.auto_ria.models.requests.RegisterAdminRequest;
import com.example.auto_ria.models.requests.RegisterManagerRequest;
import com.example.auto_ria.models.requests.RegisterUserRequest;
import com.example.auto_ria.models.responses.auth.AuthenticationResponse;
import com.example.auto_ria.models.user.AdministratorSQL;
import com.example.auto_ria.models.user.ManagerSQL;
import com.example.auto_ria.models.user.UserSQL;
import com.example.auto_ria.services.CommonService;
import com.example.auto_ria.services.user.AdministratorServiceMySQL;
import com.example.auto_ria.services.user.ManagerServiceMySQL;
import com.example.auto_ria.services.user.UsersServiceMySQLImpl;

import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AuthenticationService {

    private JwtService jwtService;
    private UserDaoSQL userDaoSQL;
    private ManagerDaoSQL managerDaoSQL;
    private AdministratorDaoSQL administratorDaoSQL;
    private AdministratorServiceMySQL administratorServiceMySQL;
    private CommonService commonService;

    private UsersServiceMySQLImpl usersServiceMySQL;
    private ManagerServiceMySQL managerServiceMySQL;

    private UserAuthDaoSQL userAuthDaoSQL;
    private AdminAuthDaoSQL adminAuthDaoSQL;
    private ManagerAuthDaoSQL managerAuthDaoSQL;

    private UserAuthenticationProvider sellerAuthenticationManager;
    private ManagerAuthenticationProvider managerAuthenticationManager;
    private RegisterKeyDaoSQL registerKeyDaoSQL;
    private AdminAuthenticationProvider adminAuthenticationProvider;

    private PasswordEncoder passwordEncoder;
    private FMService mailer;

    // ----------------------------- mix seller/customer
    public ResponseEntity<String> registerUser(RegisterUserRequest registerRequest) {
        try {

            UserSQL user = UserSQL.userBuilder()
                    .name(registerRequest.getName())
                    .lastName(registerRequest.getLastName())
                    .email(registerRequest.getEmail())
                    .password(passwordEncoder.encode(registerRequest.getPassword()))
                    .roles(List.of(ERole.USER)) // seller person none
                    .avatar(registerRequest.getAvatar())
                    .city(registerRequest.getCity())
                    .region(registerRequest.getRegion())
                    .number(registerRequest.getNumber())
                    .build();

            user.setIsActivated(false);
            userDaoSQL.save(user);

            System.out.println("***********************" + user);
            System.out.println("***********************" + user.getAuthorities());

            String activateToken = jwtService.generateRegisterKey(
                    user.getEmail(), ERole.USER, ETokenRole.USER_ACTIVATE);

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
    // -----------------------------

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

    public ResponseEntity<AuthenticationResponse> registerManager(RegisterManagerRequest registerRequest, String key) {
        try {

            System.out.println(registerRequest);
            ManagerSQL manager = ManagerSQL
                    .managerSQLBuilder()
                    .name(registerRequest.getName())
                    .lastName(registerRequest.getLastName())
                    .email(registerRequest.getEmail())
                    .avatar(registerRequest.getAvatar())
                    .password(passwordEncoder.encode(registerRequest.getPassword()))
                    .roles(List.of(ERole.MANAGER, ERole.MANAGER_GLOBAL))
                    .build();

            AuthenticationResponse authenticationResponse = jwtService.generateManagerTokenPair(manager);

            manager.setIsActivated(true);
            managerDaoSQL.save(manager);

            managerAuthDaoSQL.save(AuthSQL.builder().role(ERole.MANAGER).personId(manager.getId())
                    .accessToken(authenticationResponse.getAccessToken())
                    .refreshToken(authenticationResponse.getRefreshToken()).build());

            Map<String, Object> args = new HashMap<>();
            args.put("name", manager.getName() + manager.getLastName());
            mailer.sendEmail(manager.getEmail(), EMail.WELCOME, args);

            registerKeyDaoSQL.delete(registerKeyDaoSQL.findByRegisterKey(key));

            return ResponseEntity.ok(authenticationResponse);
        } catch (Exception e) {
            System.out.println("e.getMessage()");
            System.out.println(e.getMessage());
            throw new CustomException("Failed register", HttpStatus.EXPECTATION_FAILED);
        }
    }

    public ResponseEntity<String> codeAdmin(String email, String code) {
        try {
            System.out.println(215);
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

    public ResponseEntity<AuthenticationResponse> registerAdmin(RegisterAdminRequest registerRequest, String key) {
        try {

            AdministratorSQL administrator = AdministratorSQL
                    .adminBuilder()
                    .name(registerRequest.getName())
                    .email(registerRequest.getEmail())
                    .avatar(registerRequest.getAvatar())
                    .password(passwordEncoder.encode(registerRequest.getPassword()))
                    .lastName(registerRequest.getLastName())
                    .roles(List.of(ERole.ADMIN, ERole.ADMIN_GLOBAL))
                    .build();

            AuthenticationResponse authenticationResponse = jwtService.generateAdminTokenPair(administrator);

            administrator.setIsActivated(true);
            administratorDaoSQL.save(administrator);

            System.out.println("***********************" + administrator);
            System.out.println("***********************" + administrator.getAuthorities());

            adminAuthDaoSQL.save(AuthSQL.builder().role(ERole.ADMIN).personId(administrator.getId())
                    .accessToken(authenticationResponse.getAccessToken())
                    .refreshToken(authenticationResponse.getRefreshToken()).build());

            Map<String, Object> args = new HashMap<>();
            args.put("name", administrator.getName() + " " + administrator.getLastName());
            mailer.sendEmail(administrator.getEmail(), EMail.WELCOME, args);

            if (key != "404E745266556A586E327234538762F413F4428472B4B625064536756685921") {
                registerKeyDaoSQL.delete(registerKeyDaoSQL.findByRegisterKey(key));
            }

            return ResponseEntity.ok(authenticationResponse);
        } catch (Exception e) {
            throw new CustomException("Failed register: " + e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    public AuthenticationResponse login(LoginRequest loginRequest) {
        try {
            UserSQL user = userDaoSQL.findUserByEmail(loginRequest.getEmail());

            if (user == null) {
                throw new CustomException("User not found", HttpStatus.NOT_FOUND);
            }
            System.out.println(
                    "-------------------" + user.getEmail() + user.getAuthorities() + loginRequest.getPassword());

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

    public AuthenticationResponse loginManager(LoginRequest loginRequest) {
        try {
            ManagerSQL user = managerDaoSQL.findByEmail(loginRequest.getEmail());

            if (user == null) {
                throw new CustomException("User not found", HttpStatus.NOT_FOUND);
            }
            System.out.println(
                    "-------------------" + user.getEmail() + user.getAuthorities() + loginRequest.getPassword());

            try {
                managerAuthenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                user.getEmail(),
                                loginRequest.getPassword(),
                                user.getAuthorities()));
            } catch (Exception e) {
                throw new CustomException("Login or password is not valid", HttpStatus.BAD_REQUEST);
            }

            if (!user.getIsActivated()) {
                throw new CustomException("Activate your account to access secured endpoints", HttpStatus.FORBIDDEN);
            }

            AuthenticationResponse tokenPair = jwtService.generateManagerTokenPair(user);

            managerAuthDaoSQL.save(
                    AuthSQL.builder().role(ERole.MANAGER).personId(user.getId()).accessToken(tokenPair.getAccessToken())
                            .refreshToken(tokenPair.getRefreshToken()).build());

            managerDaoSQL.save(user);

            return tokenPair;
        } catch (CustomException e) {
            throw new CustomException("Failed login: " + e.getMessage(), e.getStatus());
        } catch (Exception e) {
            throw new CustomException("Failed login: " + e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    public AuthenticationResponse loginAdmin(LoginRequest loginRequest) {
        try {
            System.out.println("login admin-------------------");
            AdministratorSQL administrator = administratorDaoSQL.findByEmail(loginRequest.getEmail());
            System.out.println("admin-------------------" + administrator);

            if (administrator == null) {
                throw new CustomException("User not found", HttpStatus.NOT_FOUND);
            }

            System.out.println("-------------------" + administrator.getEmail() + loginRequest.getPassword());

            try {
                adminAuthenticationProvider.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                administrator.getEmail(),
                                loginRequest.getPassword(),
                                administrator.getAuthorities()));
            } catch (Exception e) {
                throw new CustomException("Login or password is not valid", HttpStatus.BAD_REQUEST);
            }

            if (!administrator.getIsActivated()) {
                throw new CustomException("Activate your account to access secured endpoints", HttpStatus.FORBIDDEN);
            }

            AuthenticationResponse tokenPair = jwtService.generateAdminTokenPair(administrator);

            adminAuthDaoSQL.save(AuthSQL.builder().role(ERole.ADMIN).personId(administrator.getId())
                    .accessToken(tokenPair.getAccessToken())
                    .refreshToken(tokenPair.getRefreshToken()).build());

            administratorDaoSQL.save(administrator);

            return tokenPair;
        } catch (CustomException e) {
            throw new CustomException("Failed login: " + e.getMessage(), e.getStatus());
        } catch (Exception e) {
            throw new CustomException("Failed login: " + e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    // public AuthenticationResponse loginCustomer(LoginRequest loginRequest) {
    // try {
    // CustomerSQL customerSQL =
    // customerDaoSQL.findByEmail(loginRequest.getEmail());

    // if (customerSQL == null) {
    // throw new CustomException("User not found", HttpStatus.NOT_FOUND);
    // }

    // try {
    // customerAuthenticationProvider.authenticate(
    // new UsernamePasswordAuthenticationToken(
    // loginRequest.getEmail(),
    // loginRequest.getPassword(),
    // customerSQL.getAuthorities()));
    // } catch (Exception e) {
    // throw new CustomException("Login or password is not valid",
    // HttpStatus.BAD_REQUEST);
    // }

    // if (!customerSQL.getIsActivated()) {
    // throw new CustomException("Activate your account to access secured
    // endpoints", HttpStatus.FORBIDDEN);
    // }

    // AuthenticationResponse tokenPair =
    // jwtService.generateCustomerTokenPair(customerSQL);

    // customerAuthDaoSQL.save(AuthSQL.builder().role(ERole.CUSTOMER).personId(customerSQL.getId())
    // .accessToken(tokenPair.getAccessToken())
    // .refreshToken(tokenPair.getRefreshToken()).build());

    // customerDaoSQL.save(customerSQL);

    // return AuthenticationResponse.builder()
    // .accessToken(tokenPair.getAccessToken())
    // .refreshToken(tokenPair.getRefreshToken()).build();
    // } catch (CustomException e) {
    // throw new CustomException("Failed login: " + e.getMessage(), e.getStatus());
    // } catch (Exception e) {
    // throw new CustomException("Failed login: " + e.getMessage(),
    // HttpStatus.CONFLICT);
    // }
    // }

    // public AuthenticationResponse refresh(RefreshRequest refreshRequest) {
    // try {
    // String refreshToken = refreshRequest.getRefreshToken();
    // String username = jwtService.extractUsername(refreshToken,
    // ETokenRole.SELLER);

    // if (!usersServiceMySQL.getByEmail(username).getIsActivated().equals(true)) {
    // throw new CustomException("Account is inactivated", HttpStatus.FORBIDDEN);
    // }

    // SellerSQL user = sellerDaoSQL.findSellerByEmail(username);

    // if (!user.getIsActivated()) {
    // throw new CustomException("Activate your account to access secured
    // endpoints", HttpStatus.FORBIDDEN);
    // }

    // String newAccessToken;
    // String newRefreshToken;

    // if (sellerAuthDaoSQL.findByRefreshToken(refreshToken) != null) {
    // newAccessToken = jwtService.generateToken(user);
    // newRefreshToken = jwtService.generateRefreshToken(user);
    // sellerDaoSQL.save(user);
    // } else {
    // throw new CustomException("Refresh token is invalid", HttpStatus.FORBIDDEN);
    // }

    // sellerAuthDaoSQL.delete(sellerAuthDaoSQL.findByRefreshToken(refreshToken));

    // sellerAuthDaoSQL
    // .save(AuthSQL.builder().role(ERole.SELLER).personId(user.getId()).accessToken(newAccessToken)
    // .refreshToken(newRefreshToken).build());

    // return
    // AuthenticationResponse.builder().accessToken(newAccessToken).refreshToken(newRefreshToken).build();
    // } catch (CustomException e) {
    // throw new CustomException("Failed refresh: " + e.getMessage(),
    // e.getStatus());
    // } catch (Exception e) {
    // throw new CustomException("Failed refresh: " + e.getMessage(),
    // HttpStatus.CONFLICT);
    // }
    // }

    // public AuthenticationResponse refreshManager(RefreshRequest refreshRequest) {
    // try {
    // String refreshToken = refreshRequest.getRefreshToken();

    // String username = jwtService.extractUsername(refreshToken,
    // ETokenRole.MANAGER);

    // ManagerSQL user = managerDaoSQL.findByEmail(username);

    // if (!user.getIsActivated().equals(true)) {
    // throw new CustomException("Account is inactivated", HttpStatus.FORBIDDEN);
    // }

    // if (!user.getIsActivated()) {
    // throw new CustomException("Activate your account to access secured
    // endpoints", HttpStatus.FORBIDDEN);
    // }

    // AuthenticationResponse tokenPair;

    // if (managerAuthDaoSQL.findByRefreshToken(refreshToken) != null) {
    // tokenPair = jwtService.generateManagerTokenPair(user);
    // managerDaoSQL.save(user);
    // } else {
    // throw new CustomException("Refresh token is invalid", HttpStatus.FORBIDDEN);
    // }

    // managerAuthDaoSQL.delete(managerAuthDaoSQL.findByRefreshToken(refreshToken));

    // managerAuthDaoSQL.save(
    // AuthSQL.builder().role(ERole.MANAGER).personId(user.getId()).accessToken(tokenPair.getAccessToken())
    // .refreshToken(tokenPair.getRefreshToken()).build());

    // return AuthenticationResponse.builder()
    // .accessToken(tokenPair.getAccessToken())
    // .refreshToken(tokenPair.getRefreshToken()).build();
    // } catch (CustomException e) {
    // throw new CustomException("Failed refresh: " + e.getMessage(),
    // e.getStatus());
    // } catch (Exception e) {
    // throw new CustomException("Failed refresh: " + e.getMessage(),
    // HttpStatus.CONFLICT);
    // }
    // }

    // public AuthenticationResponse refreshAdmin(RefreshRequest refreshRequest) {
    // try {
    // String refreshToken = refreshRequest.getRefreshToken();
    // String username = jwtService.extractUsername(refreshToken, ETokenRole.ADMIN);
    // AdministratorSQL administrator = administratorDaoSQL.findByEmail(username);

    // if (!administrator.getIsActivated().equals(true)) {
    // throw new CustomException("Account is inactivated", HttpStatus.FORBIDDEN);
    // }

    // if (!administrator.getIsActivated()) {
    // throw new CustomException("Activate your account to access secured
    // endpoints", HttpStatus.FORBIDDEN);
    // }

    // AuthenticationResponse tokenPair;

    // if (adminAuthDaoSQL.findByRefreshToken(refreshToken) != null) {
    // tokenPair = jwtService.generateAdminTokenPair(administrator);
    // administratorDaoSQL.save(administrator);
    // } else {
    // throw new CustomException("Refresh token is invalid", HttpStatus.FORBIDDEN);
    // }

    // adminAuthDaoSQL.delete(adminAuthDaoSQL.findByRefreshToken(refreshToken));

    // adminAuthDaoSQL.save(AuthSQL.builder().role(ERole.ADMIN).personId(administrator.getId())
    // .accessToken(tokenPair.getAccessToken())
    // .refreshToken(tokenPair.getRefreshToken()).build());

    // return AuthenticationResponse.builder()
    // .accessToken(tokenPair.getAccessToken())
    // .refreshToken(tokenPair.getRefreshToken()).build();
    // } catch (CustomException e) {
    // throw new CustomException("Failed refresh: " + e.getMessage(),
    // e.getStatus());
    // } catch (Exception e) {
    // throw new CustomException("Failed refresh: " + e.getMessage(),
    // HttpStatus.CONFLICT);
    // }
    // }

    // public AuthenticationResponse refreshCustomer(RefreshRequest refreshRequest)
    // {
    // try {
    // String refreshToken = refreshRequest.getRefreshToken();
    // String username = jwtService.extractUsername(refreshToken,
    // ETokenRole.CUSTOMER);

    // if
    // (!customersServiceMySQL.getByEmail(username).getIsActivated().equals(true)) {
    // throw new CustomException("Account is inactivated", HttpStatus.FORBIDDEN);
    // }

    // CustomerSQL customerSQL = customerDaoSQL.findByEmail(username);

    // if (!customerSQL.getIsActivated()) {
    // throw new CustomException("Activate your account to access secured
    // endpoints", HttpStatus.FORBIDDEN);
    // }

    // AuthenticationResponse tokenPair;

    // if (customerAuthDaoSQL.findByRefreshToken(refreshToken) != null) {
    // tokenPair = jwtService.generateCustomerTokenPair(customerSQL);
    // customerDaoSQL.save(customerSQL);
    // } else {
    // throw new CustomException("Refresh token is invalid", HttpStatus.FORBIDDEN);
    // }

    // customerAuthDaoSQL.delete(customerAuthDaoSQL.findByRefreshToken(refreshToken));

    // customerAuthDaoSQL.save(AuthSQL.builder().role(ERole.CUSTOMER).personId(customerSQL.getId())
    // .accessToken(tokenPair.getAccessToken())
    // .refreshToken(tokenPair.getRefreshToken()).build());

    // return AuthenticationResponse.builder()
    // .accessToken(tokenPair.getAccessToken())
    // .refreshToken(tokenPair.getRefreshToken()).build();
    // } catch (CustomException e) {
    // throw new CustomException("Failed refresh: " + e.getMessage(),
    // e.getStatus());
    // } catch (Exception e) {
    // throw new CustomException("Failed refresh: " + e.getMessage(),
    // HttpStatus.CONFLICT);
    // }
    // }

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
            } else if ((username = jwtService.extractUsername(refreshToken, ETokenRole.ADMIN)) != null &&
                    adminAuthDaoSQL.findByRefreshToken(refreshToken) != null) {
                AdministratorSQL administratorSQL = administratorDaoSQL.findByEmail(username);
                if (administratorSQL != null) {
                    tokenPair = jwtService.generateAdminTokenPair(administratorSQL);
                    adminAuthDaoSQL.deleteAllByRefreshToken(refreshToken);
                    adminAuthDaoSQL.save(AuthSQL.builder().role(ERole.ADMIN).personId(administratorSQL.getId())
                            .accessToken(tokenPair.getAccessToken())
                            .refreshToken(tokenPair.getRefreshToken()).build());
                }
            } else if ((username = jwtService.extractUsername(refreshToken, ETokenRole.MANAGER)) != null &&
                    managerAuthDaoSQL.findByRefreshToken(refreshToken) != null) {
                ManagerSQL managerSQL = managerDaoSQL.findByEmail(username);
                if (managerSQL != null) {
                    tokenPair = jwtService.generateManagerTokenPair(managerSQL);
                    managerAuthDaoSQL.deleteAllByRefreshToken(refreshToken);
                    managerAuthDaoSQL.save(AuthSQL.builder().role(ERole.MANAGER).personId(managerSQL.getId())
                            .accessToken(tokenPair.getAccessToken())
                            .refreshToken(tokenPair.getRefreshToken())
                            .build());
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

            ERole role = commonService.findRoleByEmail(email);

            if (role == null) {
                throw new CustomException("User not found", HttpStatus.BAD_REQUEST);
            }

            String code = jwtService.generateRegisterKey(email, role, ETokenRole.FORGOT_PASSWORD);
            registerKeyDaoSQL.save(RegisterKey.builder().registerKey(code).build());

            Map<String, Object> args = new HashMap<>();
            args.put("email", email);
            args.put("time", LocalDateTime.now());
            args.put("code", code);
            mailer.sendEmail(email, EMail.FORGOT_PASSWORD, args);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new CustomException("Forgot password error" + e.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }
    }

    public AuthenticationResponse resetPassword(String email, String owner, String encoded) {
        AuthenticationResponse authenticationResponse;

        try {
            if (ERole.ADMIN.equals(ERole.valueOf(owner))) {

                AdministratorSQL administratorSQL = administratorServiceMySQL.getByEmail(email);
                administratorSQL.setPassword(encoded);
                administratorDaoSQL.save(administratorSQL);
                adminAuthDaoSQL.deleteAllByPersonId(administratorSQL.getId());

                AuthenticationResponse authentication = jwtService.generateAdminTokenPair(administratorSQL);
                adminAuthDaoSQL.save(AuthSQL.builder().role(ERole.ADMIN)
                        .accessToken(authentication.getAccessToken())
                        .refreshToken(authentication.getRefreshToken())
                        .id(administratorSQL.getId())
                        .personId(administratorSQL.getId())
                        .build());

                authenticationResponse = authentication;

            } else if (ERole.MANAGER.equals(ERole.valueOf(owner))) {

                ManagerSQL managerSQL = managerServiceMySQL.getByEmail(email);
                managerSQL.setPassword(encoded);
                managerDaoSQL.save(managerSQL);
                managerAuthDaoSQL.deleteAllByPersonId(managerSQL.getId());

                AuthenticationResponse authentication = jwtService.generateManagerTokenPair(managerSQL);
                managerAuthDaoSQL.save(AuthSQL.builder().role(ERole.MANAGER)
                        .accessToken(authentication.getAccessToken())
                        .refreshToken(authentication.getRefreshToken())
                        .id(managerSQL.getId())
                        .personId(managerSQL.getId())
                        .build());

                authenticationResponse = authentication;

            } else if (ERole.USER.equals(ERole.valueOf(owner))) {

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
            } else {
                throw new CustomException("Token is invalid for current procedure", HttpStatus.FORBIDDEN);
            }
        } catch (CustomException e) {
            throw new CustomException("Failed reset: " + e.getMessage(), e.getStatus());
        } catch (Exception e) {
            throw new CustomException("Failed reset: " + e.getMessage(), HttpStatus.CONFLICT);
        }
        return authenticationResponse; // from auth info res/ to auth res
    }

    public void signOut(String email, String owner) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.getName().equals(email)) {
                SecurityContextHolder.clearContext();
            }

            if (ERole.ADMIN.equals(ERole.valueOf(owner))) {
                AdministratorSQL administratorSQL = administratorServiceMySQL.getByEmail(email);
                adminAuthDaoSQL.deleteAllByPersonId(administratorSQL.getId());
            } else if (ERole.MANAGER.equals(ERole.valueOf(owner))) {
                ManagerSQL managerSQL = managerServiceMySQL.getByEmail(email);
                managerAuthDaoSQL.deleteAllByPersonId(managerSQL.getId());
            } else if (ERole.USER.equals(ERole.valueOf(owner))) {
                UserSQL userSQL = usersServiceMySQL.getByEmail(email);
                userAuthDaoSQL.deleteAllByPersonId(userSQL.getId());
            } else {
                throw new CustomException("Something went wrong...", HttpStatus.BAD_REQUEST);
            }
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
