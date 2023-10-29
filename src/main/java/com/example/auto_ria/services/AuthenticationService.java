package com.example.auto_ria.services;

import com.example.auto_ria.configurations.providers.AdminAuthenticationProvider;
import com.example.auto_ria.configurations.providers.CustomerAuthenticationProvider;
import com.example.auto_ria.configurations.providers.ManagerAuthenticationProvider;
import com.example.auto_ria.configurations.providers.SellerAuthenticationProvider;
import com.example.auto_ria.dao.*;
import com.example.auto_ria.dao.authDao.AdminAuthDaoSQL;
import com.example.auto_ria.dao.authDao.CustomerAuthDaoSQL;
import com.example.auto_ria.dao.authDao.ManagerAuthDaoSQL;
import com.example.auto_ria.dao.authDao.SellerAuthDaoSQL;
import com.example.auto_ria.enums.EMail;
import com.example.auto_ria.enums.ERole;
import com.example.auto_ria.enums.ETokenRole;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.mail.FMService;
import com.example.auto_ria.models.AdministratorSQL;
import com.example.auto_ria.models.CustomerSQL;
import com.example.auto_ria.models.ManagerSQL;
import com.example.auto_ria.models.SellerSQL;
import com.example.auto_ria.models.auth.AuthSQL;
import com.example.auto_ria.models.auth.RegisterKey;
import com.example.auto_ria.models.requests.*;
import com.example.auto_ria.models.responses.AuthenticationResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
    private FMService mailer;


    public ResponseEntity<String> register(RegisterSellerRequest registerRequest) {


        if (sellerDaoSQL.findSellerByEmail(registerRequest.getEmail()) != null) {
            throw new CustomException("User with this email already exists", HttpStatus.BAD_REQUEST);
        }

        if (sellerDaoSQL.findSellerByNumber(registerRequest.getNumber()) != null) {
            throw new CustomException("User with this number already exists", HttpStatus.BAD_REQUEST);
        }

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

        String activateToken = jwtService.generateRegistrationCode(new HashMap<>(),
                seller.getEmail(), ETokenRole.SELLER_ACTIVATE);

        registerKeyDaoSQL.save(RegisterKey.builder().registerKey(activateToken).build());

        HashMap<String, Object> variables = new HashMap<>();
        variables.put("name", registerRequest.getName());
        variables.put("role", ETokenRole.SELLER);
        variables.put("code", activateToken);

        try {
            mailer.sendEmail(registerRequest.getEmail(), EMail.REGISTER_KEY, variables);
        } catch (Exception ignore) {
            sellerDaoSQL.delete(seller);
            throw new CustomException("Something went wrong... Try again later", HttpStatus.CONFLICT);
        }

        return ResponseEntity.ok("Check your email for activation");
    }

    public ResponseEntity<AuthenticationResponse> activateSeller(String email, String code) {
        try {
            SellerSQL sellerSQL = sellerDaoSQL.findSellerByEmail(email);
            sellerSQL.setIsActivated(true);

            String access = jwtService.generateToken(sellerSQL);
            String refresh = jwtService.generateRefreshToken(sellerSQL);

            sellerDaoSQL.save(sellerSQL);

            sellerAuthDaoSQL.save(AuthSQL.builder().
                    personId(sellerSQL.getId()).accessToken(access).refreshToken(refresh).build());

            registerKeyDaoSQL.delete(registerKeyDaoSQL.findByRegisterKey(code));

            Map<String, Object> vars = new HashMap<>();
            vars.put("name", sellerSQL.getName());

            mailer.sendEmail(sellerSQL.getEmail(), EMail.WELCOME, vars);

            return ResponseEntity.ok(AuthenticationResponse.builder()
                    .accessToken(access)
                    .refreshToken(refresh)
                    .build());
        } catch (Exception e) {
            throw new CustomException("Failed activation: " + e.getMessage(), HttpStatus.CONFLICT);
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
            throw new CustomException("Error creating manager code:" + e.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }
    }

    public ResponseEntity<AuthenticationResponse> registerManager(RegisterManagerRequest registerRequest, String key) {

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

        managerAuthDaoSQL.save(AuthSQL.builder().
                personId(manager.getId()).accessToken(authenticationResponse.getAccessToken())
                .refreshToken(authenticationResponse.getRefreshToken()).build());


        registerKeyDaoSQL.delete(registerKeyDaoSQL.findByRegisterKey(key));

        mailer.sendEmail(manager.getEmail(), EMail.WELCOME, new HashMap<>()); //todo add variables

        return ResponseEntity.ok(authenticationResponse);
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

        adminAuthDaoSQL.save(AuthSQL.builder().
                personId(administrator.getId()).accessToken(authenticationResponse.getAccessToken())
                .refreshToken(authenticationResponse.getRefreshToken()).build());

        mailer.sendEmail(administrator.getEmail(), EMail.WELCOME, new HashMap<>()); //todo add variables

        return ResponseEntity.ok(authenticationResponse);
    }

    public ResponseEntity<String> registerCustomer(RegisterCustomerRequest registerRequest) {

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

        String activateToken = jwtService.generateRegisterKey(customerSQL.getEmail(), ETokenRole.CUSTOMER_ACTIVATE);
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

        customerSQL.setIsActivated(false);
        customerDaoSQL.save(customerSQL);

        return ResponseEntity.ok("Check your email for activation");
    }

    public ResponseEntity<AuthenticationResponse> activateCustomer(String email, String code) {
        try {
            CustomerSQL customerSQL = customerDaoSQL.findByEmail(email);
            customerSQL.setIsActivated(true);

            String access = jwtService.generateToken(customerSQL);
            String refresh = jwtService.generateRefreshToken(customerSQL);

            customerSQL.setIsActivated(true);

            customerDaoSQL.save(customerSQL);

            customerAuthDaoSQL.save(AuthSQL.builder().
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

        sellerAuthDaoSQL.save(AuthSQL.builder().
                personId(user.getId()).accessToken(access_token)
                .refreshToken(refreshToken).build());

        sellerDaoSQL.save(user);

        return AuthenticationResponse.builder().accessToken(access_token).refreshToken(refreshToken).build();
    }

    public AuthenticationResponse loginManager(LoginRequest loginRequest) {
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

        managerAuthDaoSQL.save(AuthSQL.builder().
                personId(user.getId()).accessToken(tokenPair.getAccessToken())
                .refreshToken(tokenPair.getRefreshToken()).build());

        managerDaoSQL.save(user);

        return AuthenticationResponse.builder().accessToken(tokenPair.getAccessToken()).refreshToken(tokenPair.getRefreshToken()).build();
    }

    public AuthenticationResponse loginAdmin(LoginRequest loginRequest) {
        AdministratorSQL administrator = administratorDaoSQL.findByEmail(loginRequest.getEmail());

        if (administrator == null) {
            throw new CustomException("User not found", HttpStatus.NOT_FOUND);
        }

        try {
            adminAuthenticationProvider.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
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

        adminAuthDaoSQL.save(AuthSQL.builder().
                personId(administrator.getId()).accessToken(tokenPair.getAccessToken())
                .refreshToken(tokenPair.getRefreshToken()).build());

        administratorDaoSQL.save(administrator);

        return AuthenticationResponse.builder().accessToken(tokenPair.getAccessToken()).refreshToken(tokenPair.getRefreshToken()).build();
    }

    public AuthenticationResponse loginCustomer(LoginRequest loginRequest) {
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

        customerAuthDaoSQL.save(AuthSQL.builder().
                personId(customerSQL.getId()).accessToken(tokenPair.getAccessToken())
                .refreshToken(tokenPair.getRefreshToken()).build());

        customerDaoSQL.save(customerSQL);

        return AuthenticationResponse.builder().accessToken(tokenPair.getAccessToken()).refreshToken(tokenPair.getRefreshToken()).build();
    }

    public AuthenticationResponse refresh(RefreshSellerRequest refreshRequest) {
        String refreshToken = refreshRequest.getRefreshToken();
        String username = jwtService.extractUsername(refreshToken, ETokenRole.SELLER);

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

        sellerAuthDaoSQL.save(AuthSQL.builder().
                personId(user.getId()).accessToken(newAccessToken)
                .refreshToken(newRefreshToken).build());

        return AuthenticationResponse.builder().accessToken(newAccessToken).refreshToken(newRefreshToken).build();
    }

    public AuthenticationResponse refreshManager(RefreshSellerRequest refreshRequest) {
        String refreshToken = refreshRequest.getRefreshToken();

        String username = jwtService.extractUsername(refreshToken, ETokenRole.MANAGER);

        ManagerSQL user = managerDaoSQL.findByEmail(username);

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

        managerAuthDaoSQL.save(AuthSQL.builder().
                personId(user.getId()).accessToken(tokenPair.getAccessToken())
                .refreshToken(tokenPair.getRefreshToken()).build());

        return AuthenticationResponse.builder().accessToken(tokenPair.getAccessToken()).refreshToken(tokenPair.getRefreshToken()).build();
    }

    public AuthenticationResponse refreshAdmin(RefreshSellerRequest refreshRequest) {
        String refreshToken = refreshRequest.getRefreshToken();
        String username = jwtService.extractUsername(refreshToken, ETokenRole.ADMIN);
        AdministratorSQL administrator = administratorDaoSQL.findByEmail(username);

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

        adminAuthDaoSQL.save(AuthSQL.builder().
                personId(administrator.getId()).accessToken(tokenPair.getAccessToken())
                .refreshToken(tokenPair.getRefreshToken()).build());

        return AuthenticationResponse.builder().accessToken(tokenPair.getAccessToken()).refreshToken(tokenPair.getRefreshToken()).build();
    }

    public AuthenticationResponse refreshCustomer(RefreshSellerRequest refreshRequest) {
        String refreshToken = refreshRequest.getRefreshToken();
        String username = jwtService.extractUsername(refreshToken, ETokenRole.CUSTOMER);

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

        customerAuthDaoSQL.save(AuthSQL.builder().
                personId(customerSQL.getId()).accessToken(tokenPair.getAccessToken())
                .refreshToken(tokenPair.getRefreshToken()).build());

        return AuthenticationResponse.builder().accessToken(tokenPair.getAccessToken()).refreshToken(tokenPair.getRefreshToken()).build();
    }

    public void forgotPassword(String email) {
        try {
            String code = jwtService.generateRegisterKey(email, ETokenRole.FORGOT_PASSWORD);

            Map<String, Object> args = new HashMap<>();
            args.put("email", email);
            args.put("time", LocalDate.now());
            args.put("code", code);

            mailer.sendEmail(email, EMail.FORGOT_PASSWORD, args);
        } catch (Exception e) {
            throw new CustomException(e.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }
    }

    public void resetPassword(String email, String owner, String encoded) {

        if (ERole.ADMIN.equals(ERole.valueOf(owner))) {
            AdministratorSQL administratorSQL = administratorServiceMySQL.getByEmail(email);
            administratorSQL.setPassword(encoded);
            administratorDaoSQL.save(administratorSQL);
            adminAuthDaoSQL.deleteAllByPersonId(administratorSQL.getId());
        } else if (ERole.MANAGER.equals(ERole.valueOf(owner))) {
            ManagerSQL managerSQL = managerServiceMySQL.getByEmail(email);
            managerSQL.setPassword(encoded);
            managerDaoSQL.save(managerSQL);
            managerAuthDaoSQL.deleteAllByPersonId(managerSQL.getId());
        } else if (ERole.SELLER.equals(ERole.valueOf(owner))) {
            SellerSQL sellerSQL = usersServiceMySQL.getByEmail(email);
            sellerSQL.setPassword(encoded);
            userDaoSQL.save(sellerSQL);
            sellerAuthDaoSQL.deleteAllByPersonId(sellerSQL.getId());
        } else if (ERole.CUSTOMER.equals(ERole.valueOf(owner))) {
            CustomerSQL customerSQL = customersServiceMySQL.getByEmail(email);
            customerSQL.setPassword(encoded);
            customerDaoSQL.save(customerSQL);
            customerAuthDaoSQL.deleteAllByPersonId(customerSQL.getId());
        } else {
            throw new CustomException("Token is invalid for current procedure", HttpStatus.FORBIDDEN);
        }
    }

    public void signOut(String email, String owner) {
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
    }


    public String checkRegistrationKey(HttpServletRequest request, String email, ETokenRole role) {
        String authorizationHeader = request.getHeader("Register-key");

        if (authorizationHeader == null) {
            throw new CustomException("Register key required", HttpStatus.FORBIDDEN);
        }

        if (registerKeyDaoSQL.findByRegisterKey(authorizationHeader) == null) {
            throw new CustomException("Key is not valid", HttpStatus.FORBIDDEN);
        }

        if (jwtService.isTokenExprired(authorizationHeader)) {
            throw new CustomException("Key expired", HttpStatus.FORBIDDEN);
        }

        if (!jwtService.extractIssuer(authorizationHeader).equals(role.name())) {
            throw new CustomException("The key is not valid for creation of " + role.name().toLowerCase(), HttpStatus.FORBIDDEN);
        }

        if (!jwtService.isKeyValid(authorizationHeader, email, ETokenRole.valueOf(role.name()))) {
            throw new CustomException("Not valid key owner", HttpStatus.FORBIDDEN);
        }

        return authorizationHeader;
    }
}
