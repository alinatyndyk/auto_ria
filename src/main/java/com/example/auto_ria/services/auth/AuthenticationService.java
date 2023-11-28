package com.example.auto_ria.services.auth;

import com.example.auto_ria.configurations.providers.AdminAuthenticationProvider;
import com.example.auto_ria.configurations.providers.CustomerAuthenticationProvider;
import com.example.auto_ria.configurations.providers.ManagerAuthenticationProvider;
import com.example.auto_ria.configurations.providers.SellerAuthenticationProvider;
import com.example.auto_ria.dao.auth.*;
import com.example.auto_ria.dao.user.AdministratorDaoSQL;
import com.example.auto_ria.dao.user.CustomerDaoSQL;
import com.example.auto_ria.dao.user.ManagerDaoSQL;
import com.example.auto_ria.dao.user.UserDaoSQL;
import com.example.auto_ria.enums.EMail;
import com.example.auto_ria.enums.ERole;
import com.example.auto_ria.enums.ETokenRole;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.mail.FMService;
import com.example.auto_ria.models.auth.AuthSQL;
import com.example.auto_ria.models.auth.RegisterKey;
import com.example.auto_ria.models.requests.*;
import com.example.auto_ria.models.responses.auth.AuthenticationInfoResponse;
import com.example.auto_ria.models.responses.auth.AuthenticationResponse;
import com.example.auto_ria.models.user.AdministratorSQL;
import com.example.auto_ria.models.user.CustomerSQL;
import com.example.auto_ria.models.user.ManagerSQL;
import com.example.auto_ria.models.user.SellerSQL;
import com.example.auto_ria.services.CommonService;
import com.example.auto_ria.services.otherApi.CitiesService;
import com.example.auto_ria.services.user.AdministratorServiceMySQL;
import com.example.auto_ria.services.user.CustomersServiceMySQL;
import com.example.auto_ria.services.user.ManagerServiceMySQL;
import com.example.auto_ria.services.user.UsersServiceMySQLImpl;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class AuthenticationService {

    private JwtService jwtService;
    private UserDaoSQL sellerDaoSQL;
    private ManagerDaoSQL managerDaoSQL;
    private AdministratorDaoSQL administratorDaoSQL;
    private CustomerDaoSQL customerDaoSQL;
    private UserDaoSQL userDaoSQL;
    private AdministratorServiceMySQL administratorServiceMySQL;
    private CommonService commonService;

    private CustomersServiceMySQL customersServiceMySQL;
    private UsersServiceMySQLImpl usersServiceMySQL;
    private ManagerServiceMySQL managerServiceMySQL;

    private SellerAuthDaoSQL sellerAuthDaoSQL;
    private CustomerAuthDaoSQL customerAuthDaoSQL;
    private AdminAuthDaoSQL adminAuthDaoSQL;
    private ManagerAuthDaoSQL managerAuthDaoSQL;

    private SellerAuthenticationProvider sellerAuthenticationManager;
    private ManagerAuthenticationProvider managerAuthenticationManager;
    private RegisterKeyDaoSQL registerKeyDaoSQL;
    private AdminAuthenticationProvider adminAuthenticationProvider;
    private CustomerAuthenticationProvider customerAuthenticationProvider;

    private PasswordEncoder passwordEncoder;
    private CitiesService citiesService;
    private FMService mailer;


    public ResponseEntity<String> register(RegisterSellerRequest registerRequest) {
        try {

            SellerSQL seller = SellerSQL
                    .sellerBuilder()
                    .name(registerRequest.getName())
                    .lastName(registerRequest.getLastName())
                    .email(registerRequest.getEmail())
                    .password(passwordEncoder.encode(registerRequest.getPassword()))
                    .roles(List.of(ERole.SELLER, ERole.SELLER_PERSON))
                    .avatar(registerRequest.getAvatar())
                    .city(registerRequest.getCity())
                    .region(registerRequest.getRegion())
                    .number(registerRequest.getNumber())
                    .build();

            seller.setIsActivated(false);
            sellerDaoSQL.save(seller);

            String activateToken = jwtService.generateRegisterKey(
                    seller.getEmail(), ERole.SELLER, ETokenRole.SELLER_ACTIVATE);

            registerKeyDaoSQL.save(RegisterKey.builder().registerKey(activateToken).build());

            HashMap<String, Object> variables = new HashMap<>();
            variables.put("name", registerRequest.getName());
            variables.put("role", ETokenRole.SELLER);
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

    public ResponseEntity<AuthenticationResponse> activateSeller(String email, String code) {
        try {
            SellerSQL sellerSQL = sellerDaoSQL.findSellerByEmail(email);

            if (sellerSQL.getIsActivated()) {
                throw new CustomException("Seller is already activated", HttpStatus.BAD_REQUEST);
            }

            sellerSQL.setIsActivated(true);

            String access = jwtService.generateToken(sellerSQL);
            String refresh = jwtService.generateRefreshToken(sellerSQL);

            sellerDaoSQL.save(sellerSQL);

            sellerAuthDaoSQL.save(AuthSQL.builder().role(ERole.SELLER)
                    .personId(sellerSQL.getId()).accessToken(access).refreshToken(refresh).build());

            registerKeyDaoSQL.delete(registerKeyDaoSQL.findByRegisterKey(code));

            Map<String, Object> vars = new HashMap<>();
            vars.put("name", sellerSQL.getName());

            mailer.sendEmail(sellerSQL.getEmail(), EMail.WELCOME, vars);

            return ResponseEntity.ok(AuthenticationResponse.builder()
                    .accessToken(access)
                    .refreshToken(refresh)
                    .build());
        } catch (Exception e) {
            throw new CustomException("Failed activation", HttpStatus.EXPECTATION_FAILED);
        }

    }

    public ResponseEntity<String> codeManager(String email, String code) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("code", code);
            map.put("role", ERole.ADMIN.name());

            mailer.sendEmail(email, EMail.REGISTER_KEY, map);

            registerKeyDaoSQL.save(RegisterKey.builder().registerKey(code).build());

            return ResponseEntity.ok("Email sent");
        } catch (Exception e) {
            throw new CustomException("Error creating manager code", HttpStatus.EXPECTATION_FAILED);
        }
    }

    public ResponseEntity<AuthenticationResponse> registerManager(RegisterManagerRequest registerRequest, String key) {
        try {
            ManagerSQL manager = ManagerSQL
                    .managerSQLBuilder()
                    .name(registerRequest.getName())
                    .email(registerRequest.getEmail())
                    .avatar(registerRequest.getAvatar())
                    .password(passwordEncoder.encode(registerRequest.getPassword()))
                    .roles(List.of(ERole.MANAGER, ERole.MANAGER_GLOBAL))
                    .build();

            AuthenticationResponse authenticationResponse = jwtService.generateManagerTokenPair(manager);

            manager.setIsActivated(true);

            managerDaoSQL.save(manager);

            managerAuthDaoSQL.save(AuthSQL.builder().role(ERole.MANAGER).
                    personId(manager.getId()).accessToken(authenticationResponse.getAccessToken())
                    .refreshToken(authenticationResponse.getRefreshToken()).build());


            registerKeyDaoSQL.delete(registerKeyDaoSQL.findByRegisterKey(key));

            Map<String, Object> args = new HashMap<>();
            args.put("name", manager.getName() + manager.getLastName());
            mailer.sendEmail(manager.getEmail(), EMail.WELCOME, args);

            return ResponseEntity.ok(authenticationResponse);
        } catch (Exception e) {
            throw new CustomException("Failed register", HttpStatus.EXPECTATION_FAILED);
        }
    }

    public ResponseEntity<String> codeAdmin(String email, String code) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("code", code);
            map.put("role", ERole.ADMIN.name());

            mailer.sendEmail(email, EMail.REGISTER_KEY, map);

            registerKeyDaoSQL.save(RegisterKey.builder().registerKey(code).build());

            return ResponseEntity.ok("Email sent");
        } catch (Exception e) {
            throw new CustomException("Error creating admin code: " + e.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }
    }

    public ResponseEntity<AuthenticationResponse> registerAdmin(RegisterAdminRequest registerRequest) {
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

            adminAuthDaoSQL.save(AuthSQL.builder().role(ERole.ADMIN).
                    personId(administrator.getId()).accessToken(authenticationResponse.getAccessToken())
                    .refreshToken(authenticationResponse.getRefreshToken()).build());

            Map<String, Object> args = new HashMap<>();
            args.put("name", administrator.getName() + " " + administrator.getLastName());

            mailer.sendEmail(administrator.getEmail(), EMail.WELCOME, args);

            return ResponseEntity.ok(authenticationResponse);
        } catch (Exception e) {
            throw new CustomException("Failed register: " + e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    public ResponseEntity<String> registerCustomer(RegisterCustomerRequest registerRequest) {
        try {

            citiesService.isValidUkrainianCity(registerRequest.getRegion(), registerRequest.getCity());

            CustomerSQL customerSQL = CustomerSQL
                    .customerBuilder()
                    .name(registerRequest.getName())
                    .lastName(registerRequest.getLastName())
                    .city(registerRequest.getCity())
                    .region(registerRequest.getRegion())
                    .email(registerRequest.getEmail())
                    .avatar(registerRequest.getAvatar())
                    .password(passwordEncoder.encode(registerRequest.getPassword()))
                    .roles(List.of(ERole.CUSTOMER))
                    .build();

            customerSQL.setIsActivated(false);

            String activateToken = jwtService.generateRegisterKey(customerSQL.getEmail(), ERole.CUSTOMER, ETokenRole.CUSTOMER_ACTIVATE);
            registerKeyDaoSQL.save(RegisterKey.builder().registerKey(activateToken).build());

            HashMap<String, Object> variables = new HashMap<>();
            variables.put("name", registerRequest.getName());
            variables.put("role", ETokenRole.CUSTOMER);
            variables.put("code", activateToken);

            try {
                mailer.sendEmail(registerRequest.getEmail(), EMail.REGISTER_KEY, variables);
            } catch (Exception ignore) {
                customerDaoSQL.delete(customerSQL);
                throw new CustomException("Something went wrong... Try again later", HttpStatus.CONFLICT);
            }

            customerDaoSQL.save(customerSQL);

            return ResponseEntity.ok("Check your email for activation");
        } catch (CustomException e) {
            throw new CustomException("Failed register: " + e.getMessage(), e.getStatus());
        } catch (Exception e) {
            throw new CustomException("Failed register: " + e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    public ResponseEntity<AuthenticationResponse> activateCustomer(String email, String code) {
        try {
            CustomerSQL customerSQL = customerDaoSQL.findByEmail(email);

            if (customerSQL.getIsActivated()) {
                throw new CustomException("Customer is already activated", HttpStatus.BAD_REQUEST);
            }

            customerSQL.setIsActivated(true);

            String access = jwtService.generateToken(customerSQL);
            String refresh = jwtService.generateRefreshToken(customerSQL);

            customerSQL.setIsActivated(true);

            customerDaoSQL.save(customerSQL);

            customerAuthDaoSQL.save(AuthSQL.builder().role(ERole.CUSTOMER).
                    personId(customerSQL.getId()).accessToken(access).refreshToken(refresh).build());


            registerKeyDaoSQL.delete(registerKeyDaoSQL.findByRegisterKey(code));

            Map<String, Object> vars = new HashMap<>();
            vars.put("name", customerSQL.getName());

            mailer.sendEmail(customerSQL.getEmail(), EMail.WELCOME, vars);

            return ResponseEntity.ok(AuthenticationResponse.builder()
                    .accessToken(access)
                    .refreshToken(refresh)
                    .build());
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }

    }


    public AuthenticationResponse login(LoginRequest loginRequest) {
        try {
            SellerSQL user = sellerDaoSQL.findSellerByEmail(loginRequest.getEmail());

            if (user == null) {
                throw new CustomException("User not found", HttpStatus.NOT_FOUND);
            }
            try {
                sellerAuthenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                user.getEmail(),
                                loginRequest.getPassword(),
                                user.getAuthorities()
                        )
                );

            } catch (Exception e) {
                throw new CustomException("Login or password is not valid", HttpStatus.BAD_REQUEST);
            }

            if (!user.getIsActivated()) {
                throw new CustomException("Activate your account to access secured endpoints", HttpStatus.FORBIDDEN);
            }

            String access_token = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            sellerAuthDaoSQL.save(AuthSQL.builder().role(ERole.SELLER).
                    personId(user.getId()).accessToken(access_token)
                    .refreshToken(refreshToken).build());

            sellerDaoSQL.save(user);

            return AuthenticationResponse.builder().accessToken(access_token).refreshToken(refreshToken).build();
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

            try {
                managerAuthenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                user.getEmail(),
                                loginRequest.getPassword(),
                                user.getAuthorities()
                        )
                );
            } catch (Exception e) {
                throw new CustomException("Login or password is not valid", HttpStatus.BAD_REQUEST);
            }

            if (!user.getIsActivated()) {
                throw new CustomException("Activate your account to access secured endpoints", HttpStatus.FORBIDDEN);
            }

            AuthenticationResponse tokenPair = jwtService.generateManagerTokenPair(user);

            managerAuthDaoSQL.save(AuthSQL.builder().role(ERole.MANAGER).
                    personId(user.getId()).accessToken(tokenPair.getAccessToken())
                    .refreshToken(tokenPair.getRefreshToken()).build());

            managerDaoSQL.save(user);

            return AuthenticationResponse.builder()
                    .accessToken(tokenPair.getAccessToken())
                    .refreshToken(tokenPair.getRefreshToken()).build();
        } catch (CustomException e) {
            throw new CustomException("Failed login: " + e.getMessage(), e.getStatus());
        } catch (Exception e) {
            throw new CustomException("Failed login: " + e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    public AuthenticationResponse loginAdmin(LoginRequest loginRequest) {
        try {
            AdministratorSQL administrator = administratorDaoSQL.findByEmail(loginRequest.getEmail());

            if (administrator == null) {
                throw new CustomException("User not found", HttpStatus.NOT_FOUND);
            }

            try {
                adminAuthenticationProvider.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                administrator.getEmail(),
                                loginRequest.getPassword(),
                                administrator.getAuthorities()
                        )
                );
            } catch (Exception e) {
                throw new CustomException("Login or password is not valid", HttpStatus.BAD_REQUEST);
            }

            if (!administrator.getIsActivated()) {
                throw new CustomException("Activate your account to access secured endpoints", HttpStatus.FORBIDDEN);
            }

            AuthenticationResponse tokenPair = jwtService.generateAdminTokenPair(administrator);

            adminAuthDaoSQL.save(AuthSQL.builder().role(ERole.ADMIN).
                    personId(administrator.getId()).accessToken(tokenPair.getAccessToken())
                    .refreshToken(tokenPair.getRefreshToken()).build());

            administratorDaoSQL.save(administrator);

            return AuthenticationResponse.builder()
                    .accessToken(tokenPair.getAccessToken())
                    .refreshToken(tokenPair.getRefreshToken()).build();
        } catch (CustomException e) {
            throw new CustomException("Failed login: " + e.getMessage(), e.getStatus());
        } catch (Exception e) {
            throw new CustomException("Failed login: " + e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    public AuthenticationResponse loginCustomer(LoginRequest loginRequest) {
        try {
            CustomerSQL customerSQL = customerDaoSQL.findByEmail(loginRequest.getEmail());

            if (customerSQL == null) {
                throw new CustomException("User not found", HttpStatus.NOT_FOUND);
            }

            try {
                customerAuthenticationProvider.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                loginRequest.getEmail(),
                                loginRequest.getPassword(),
                                customerSQL.getAuthorities()
                        )
                );
            } catch (Exception e) {
                throw new CustomException("Login or password is not valid", HttpStatus.BAD_REQUEST);
            }

            if (!customerSQL.getIsActivated()) {
                throw new CustomException("Activate your account to access secured endpoints", HttpStatus.FORBIDDEN);
            }

            AuthenticationResponse tokenPair = jwtService.generateCustomerTokenPair(customerSQL);

            customerAuthDaoSQL.save(AuthSQL.builder().role(ERole.CUSTOMER).
                    personId(customerSQL.getId()).accessToken(tokenPair.getAccessToken())
                    .refreshToken(tokenPair.getRefreshToken()).build());

            customerDaoSQL.save(customerSQL);

            return AuthenticationResponse.builder()
                    .accessToken(tokenPair.getAccessToken())
                    .refreshToken(tokenPair.getRefreshToken()).build();
        } catch (CustomException e) {
            throw new CustomException("Failed login: " + e.getMessage(), e.getStatus());
        } catch (Exception e) {
            throw new CustomException("Failed login: " + e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    public AuthenticationResponse refresh(RefreshRequest refreshRequest) {
        try {
            String refreshToken = refreshRequest.getRefreshToken();
            String username = jwtService.extractUsername(refreshToken, ETokenRole.SELLER);

            if (!usersServiceMySQL.getByEmail(username).getIsActivated().equals(true)) {
                throw new CustomException("Account is inactivated", HttpStatus.FORBIDDEN);
            }

            SellerSQL user = sellerDaoSQL.findSellerByEmail(username);

            if (!user.getIsActivated()) {
                throw new CustomException("Activate your account to access secured endpoints", HttpStatus.FORBIDDEN);
            }

            String newAccessToken;
            String newRefreshToken;

            if (sellerAuthDaoSQL.findByRefreshToken(refreshToken) != null) {
                newAccessToken = jwtService.generateToken(user);
                newRefreshToken = jwtService.generateRefreshToken(user);
                sellerDaoSQL.save(user);
            } else {
                throw new CustomException("Refresh token is invalid", HttpStatus.FORBIDDEN);
            }

            sellerAuthDaoSQL.delete(sellerAuthDaoSQL.findByRefreshToken(refreshToken));

            sellerAuthDaoSQL.save(AuthSQL.builder().role(ERole.SELLER).
                    personId(user.getId()).accessToken(newAccessToken)
                    .refreshToken(newRefreshToken).build());

            return AuthenticationResponse.builder().accessToken(newAccessToken).refreshToken(newRefreshToken).build();
        } catch (CustomException e) {
            throw new CustomException("Failed refresh: " + e.getMessage(), e.getStatus());
        } catch (Exception e) {
            throw new CustomException("Failed refresh: " + e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    public AuthenticationResponse refreshManager(RefreshRequest refreshRequest) {
        try {
            String refreshToken = refreshRequest.getRefreshToken();

            String username = jwtService.extractUsername(refreshToken, ETokenRole.MANAGER);

            ManagerSQL user = managerDaoSQL.findByEmail(username);

            if (!user.getIsActivated().equals(true)) {
                throw new CustomException("Account is inactivated", HttpStatus.FORBIDDEN);
            }

            if (!user.getIsActivated()) {
                throw new CustomException("Activate your account to access secured endpoints", HttpStatus.FORBIDDEN);
            }

            AuthenticationResponse tokenPair;

            if (managerAuthDaoSQL.findByRefreshToken(refreshToken) != null) {
                tokenPair = jwtService.generateManagerTokenPair(user);
                managerDaoSQL.save(user);
            } else {
                throw new CustomException("Refresh token is invalid", HttpStatus.FORBIDDEN);
            }

            managerAuthDaoSQL.delete(managerAuthDaoSQL.findByRefreshToken(refreshToken));

            managerAuthDaoSQL.save(AuthSQL.builder().role(ERole.MANAGER).
                    personId(user.getId()).accessToken(tokenPair.getAccessToken())
                    .refreshToken(tokenPair.getRefreshToken()).build());

            return AuthenticationResponse.builder()
                    .accessToken(tokenPair.getAccessToken())
                    .refreshToken(tokenPair.getRefreshToken()).build();
        } catch (CustomException e) {
            throw new CustomException("Failed refresh: " + e.getMessage(), e.getStatus());
        } catch (Exception e) {
            throw new CustomException("Failed refresh: " + e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    public AuthenticationResponse refreshAdmin(RefreshRequest refreshRequest) {
        try {
            String refreshToken = refreshRequest.getRefreshToken();
            String username = jwtService.extractUsername(refreshToken, ETokenRole.ADMIN);
            AdministratorSQL administrator = administratorDaoSQL.findByEmail(username);

            if (!administrator.getIsActivated().equals(true)) {
                throw new CustomException("Account is inactivated", HttpStatus.FORBIDDEN);
            }

            if (!administrator.getIsActivated()) {
                throw new CustomException("Activate your account to access secured endpoints", HttpStatus.FORBIDDEN);
            }

            AuthenticationResponse tokenPair;

            if (adminAuthDaoSQL.findByRefreshToken(refreshToken) != null) {
                tokenPair = jwtService.generateAdminTokenPair(administrator);
                administratorDaoSQL.save(administrator);
            } else {
                throw new CustomException("Refresh token is invalid", HttpStatus.FORBIDDEN);
            }

            adminAuthDaoSQL.delete(adminAuthDaoSQL.findByRefreshToken(refreshToken));

            adminAuthDaoSQL.save(AuthSQL.builder().role(ERole.ADMIN).
                    personId(administrator.getId()).accessToken(tokenPair.getAccessToken())
                    .refreshToken(tokenPair.getRefreshToken()).build());

            return AuthenticationResponse.builder()
                    .accessToken(tokenPair.getAccessToken())
                    .refreshToken(tokenPair.getRefreshToken()).build();
        } catch (CustomException e) {
            throw new CustomException("Failed refresh: " + e.getMessage(), e.getStatus());
        } catch (Exception e) {
            throw new CustomException("Failed refresh: " + e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    public AuthenticationResponse refreshCustomer(RefreshRequest refreshRequest) {
        try {
            String refreshToken = refreshRequest.getRefreshToken();
            String username = jwtService.extractUsername(refreshToken, ETokenRole.CUSTOMER);

            if (!customersServiceMySQL.getByEmail(username).getIsActivated().equals(true)) {
                throw new CustomException("Account is inactivated", HttpStatus.FORBIDDEN);
            }

            CustomerSQL customerSQL = customerDaoSQL.findByEmail(username);

            if (!customerSQL.getIsActivated()) {
                throw new CustomException("Activate your account to access secured endpoints", HttpStatus.FORBIDDEN);
            }

            AuthenticationResponse tokenPair;

            if (customerAuthDaoSQL.findByRefreshToken(refreshToken) != null) {
                tokenPair = jwtService.generateCustomerTokenPair(customerSQL);
                customerDaoSQL.save(customerSQL);
            } else {
                throw new CustomException("Refresh token is invalid", HttpStatus.FORBIDDEN);
            }

            customerAuthDaoSQL.delete(customerAuthDaoSQL.findByRefreshToken(refreshToken));

            customerAuthDaoSQL.save(AuthSQL.builder().role(ERole.CUSTOMER).
                    personId(customerSQL.getId()).accessToken(tokenPair.getAccessToken())
                    .refreshToken(tokenPair.getRefreshToken()).build());

            return AuthenticationResponse.builder()
                    .accessToken(tokenPair.getAccessToken())
                    .refreshToken(tokenPair.getRefreshToken()).build();
        } catch (CustomException e) {
            throw new CustomException("Failed refresh: " + e.getMessage(), e.getStatus());
        } catch (Exception e) {
            throw new CustomException("Failed refresh: " + e.getMessage(), HttpStatus.CONFLICT);
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

            if ((username = jwtService.extractUsername(refreshToken, ETokenRole.CUSTOMER)) != null &&
                    customerAuthDaoSQL.findByRefreshToken(refreshToken) != null) {
                CustomerSQL customerSQL = customerDaoSQL.findByEmail(username);
                if (customerSQL != null) {
                    tokenPair = jwtService.generateCustomerTokenPair(customerSQL);
                    customerAuthDaoSQL.deleteAllByRefreshToken(refreshToken);
                    customerAuthDaoSQL.save(AuthSQL.builder().role(ERole.CUSTOMER).
                            personId(customerSQL.getId()).accessToken(tokenPair.getAccessToken())
                            .refreshToken(tokenPair.getRefreshToken()).build());
                }

            } else if ((username = jwtService.extractUsername(refreshToken, ETokenRole.SELLER)) != null &&
                    sellerAuthDaoSQL.findByRefreshToken(refreshToken) != null
            ) {
                SellerSQL sellerSQL = sellerDaoSQL.findSellerByEmail(username);
                if (sellerSQL != null) {
                    tokenPair = AuthenticationResponse.builder()
                            .accessToken(jwtService.generateToken(sellerSQL))
                            .refreshToken(jwtService.generateRefreshToken(sellerSQL))
                            .build();
                    sellerAuthDaoSQL.deleteAllByRefreshToken(refreshToken);
                    sellerAuthDaoSQL.save(AuthSQL.builder().role(ERole.SELLER).
                            personId(sellerSQL.getId()).accessToken(tokenPair.getAccessToken())
                            .refreshToken(tokenPair.getRefreshToken()).build());
                }
            } else if ((username = jwtService.extractUsername(refreshToken, ETokenRole.ADMIN)) != null &&
                    adminAuthDaoSQL.findByRefreshToken(refreshToken) != null) {
                AdministratorSQL administratorSQL = administratorDaoSQL.findByEmail(username);
                if (administratorSQL != null) {
                    tokenPair = jwtService.generateAdminTokenPair(administratorSQL);
                    adminAuthDaoSQL.deleteAllByRefreshToken(refreshToken);
                    adminAuthDaoSQL.save(AuthSQL.builder().role(ERole.ADMIN).
                            personId(administratorSQL.getId()).accessToken(tokenPair.getAccessToken())
                            .refreshToken(tokenPair.getRefreshToken()).build());
                }
            } else if ((username = jwtService.extractUsername(refreshToken, ETokenRole.MANAGER)) != null &&
                    managerAuthDaoSQL.findByRefreshToken(refreshToken) != null) {
                ManagerSQL managerSQL = managerDaoSQL.findByEmail(username);
                if (managerSQL != null) {
                    tokenPair = jwtService.generateManagerTokenPair(managerSQL);
                    managerAuthDaoSQL.deleteAllByRefreshToken(refreshToken);
                    managerAuthDaoSQL.save(AuthSQL.builder().role(ERole.MANAGER).
                            personId(managerSQL.getId()).
                            accessToken(tokenPair.getAccessToken())
                            .refreshToken(tokenPair.getRefreshToken())
                            .build()); //todo use refresh Manager and not code again
                }
            }

            if (username == null) {
                throw new CustomException("Token invalid", HttpStatus.FORBIDDEN);
            }


            return AuthenticationResponse.builder()
                    .accessToken(tokenPair.getAccessToken())
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

            String code = jwtService.generateRegisterKey(email, role, ETokenRole.FORGOT_PASSWORD);
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

    public AuthenticationInfoResponse resetPassword(String email, String owner, String encoded) {
        AuthenticationInfoResponse authenticationInfoResponse;
        try {
            if (ERole.ADMIN.equals(ERole.valueOf(owner))) {
                AdministratorSQL administratorSQL = administratorServiceMySQL.getByEmail(email);
                administratorSQL.setPassword(encoded);
                administratorDaoSQL.save(administratorSQL);
                adminAuthDaoSQL.deleteAllByPersonId(administratorSQL.getId());
                AuthenticationResponse authenticationResponse = jwtService.generateAdminTokenPair(administratorSQL);
                adminAuthDaoSQL.save(AuthSQL.builder().role(ERole.ADMIN)
                        .accessToken(authenticationResponse.getAccessToken())
                        .refreshToken(authenticationResponse.getRefreshToken())
                        .id(administratorSQL.getId())
                        .build());

                authenticationInfoResponse = AuthenticationInfoResponse.builder()
                        .accessToken(authenticationResponse.getAccessToken())
                        .refreshToken(authenticationResponse.getRefreshToken())
                        .id(administratorSQL.getId())
                        .build();
            } else if (ERole.MANAGER.equals(ERole.valueOf(owner))) {
                ManagerSQL managerSQL = managerServiceMySQL.getByEmail(email);
                managerSQL.setPassword(encoded);
                managerDaoSQL.save(managerSQL);
                managerAuthDaoSQL.deleteAllByPersonId(managerSQL.getId());
                AuthenticationResponse authenticationResponse = jwtService.generateManagerTokenPair(managerSQL);
                managerAuthDaoSQL.save(AuthSQL.builder().role(ERole.MANAGER)
                        .accessToken(authenticationResponse.getAccessToken())
                        .refreshToken(authenticationResponse.getRefreshToken())
                        .id(managerSQL.getId())
                        .build());

                authenticationInfoResponse = AuthenticationInfoResponse.builder()
                        .accessToken(authenticationResponse.getAccessToken())
                        .refreshToken(authenticationResponse.getRefreshToken())
                        .id(managerSQL.getId())
                        .build();
            } else if (ERole.SELLER.equals(ERole.valueOf(owner))) {
                SellerSQL sellerSQL = usersServiceMySQL.getByEmail(email);
                System.out.println(sellerSQL);
                System.out.println(801);
                sellerSQL.setPassword(encoded);
                userDaoSQL.save(sellerSQL);
                sellerAuthDaoSQL.deleteAllByPersonId(sellerSQL.getId());
                System.out.println(806);
                AuthenticationResponse authenticationResponse = AuthenticationResponse.builder()
                        .accessToken(jwtService.generateToken(sellerSQL))
                        .refreshToken(jwtService.generateRefreshToken(sellerSQL))
                        .build();

                sellerAuthDaoSQL.save(AuthSQL.builder().role(ERole.SELLER)
                        .accessToken(authenticationResponse.getAccessToken())
                        .refreshToken(authenticationResponse.getRefreshToken())
                        .id(sellerSQL.getId())
                        .build());

                System.out.println(818);
                authenticationInfoResponse = AuthenticationInfoResponse.builder()
                        .accessToken(authenticationResponse.getAccessToken())
                        .refreshToken(authenticationResponse.getRefreshToken())
                        .id(sellerSQL.getId())
                        .build();

            } else if (ERole.CUSTOMER.equals(ERole.valueOf(owner))) {
                CustomerSQL customerSQL = customersServiceMySQL.getByEmail(email);
                customerSQL.setPassword(encoded);
                customerDaoSQL.save(customerSQL);
                customerAuthDaoSQL.deleteAllByPersonId(customerSQL.getId());
                AuthenticationResponse authenticationResponse = jwtService.generateCustomerTokenPair(customerSQL);
                customerAuthDaoSQL.save(AuthSQL.builder().role(ERole.CUSTOMER)
                        .accessToken(authenticationResponse.getAccessToken())
                        .refreshToken(authenticationResponse.getRefreshToken())
                        .id(customerSQL.getId())
                        .build());

                authenticationInfoResponse = AuthenticationInfoResponse.builder()
                        .accessToken(authenticationResponse.getAccessToken())
                        .refreshToken(authenticationResponse.getRefreshToken())
                        .id(customerSQL.getId())
                        .build();
            } else {
                throw new CustomException("Token is invalid for current procedure", HttpStatus.FORBIDDEN);
            }
        } catch (CustomException e) {
            throw new CustomException("Failed reset: " + e.getMessage(), e.getStatus());
        } catch (Exception e) {
            throw new CustomException("Failed reset: " + e.getMessage(), HttpStatus.CONFLICT);
        }
        return authenticationInfoResponse;
    }

    public void signOut(String email, String owner) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.getName().equals(email)) {
                SecurityContextHolder.clearContext();
            }

            if (ERole.ADMIN.equals(ERole.valueOf(owner))) {
                AdministratorSQL administratorSQL = administratorServiceMySQL.getByEmail(email);
                administratorDaoSQL.save(administratorSQL);
                adminAuthDaoSQL.deleteAllByPersonId(administratorSQL.getId());
            } else if (ERole.MANAGER.equals(ERole.valueOf(owner))) {
                ManagerSQL managerSQL = managerServiceMySQL.getByEmail(email);
                managerDaoSQL.save(managerSQL);
                managerAuthDaoSQL.deleteAllByPersonId(managerSQL.getId());
            } else if (ERole.SELLER.equals(ERole.valueOf(owner))) {
                SellerSQL sellerSQL = usersServiceMySQL.getByEmail(email);
                userDaoSQL.save(sellerSQL);
                sellerAuthDaoSQL.deleteAllByPersonId(sellerSQL.getId());
            } else if (ERole.CUSTOMER.equals(ERole.valueOf(owner))) {
                CustomerSQL customerSQL = customersServiceMySQL.getByEmail(email);
                customerDaoSQL.save(customerSQL);
                customerAuthDaoSQL.deleteAllByPersonId(customerSQL.getId());
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
                                       ETokenRole tokenRecognition,
                                       ETokenRole funcRecognition) {

        checkKey(authorizationHeader, tokenRecognition, funcRecognition);

        if (!jwtService.extractIssuer(authorizationHeader).equals(role.name())) {
            throw new CustomException("The key is not valid for creation of " + role.name().toLowerCase(), HttpStatus.FORBIDDEN);
        }

        if (!jwtService.isKeyValid(authorizationHeader, email, ETokenRole.valueOf(role.name()))) {
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
