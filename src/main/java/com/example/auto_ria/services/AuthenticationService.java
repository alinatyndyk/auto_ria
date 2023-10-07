package com.example.auto_ria.services;

import com.example.auto_ria.configurations.providers.AdminAuthenticationProvider;
import com.example.auto_ria.configurations.providers.CustomerAuthenticationProvider;
import com.example.auto_ria.configurations.providers.ManagerAuthenticationProvider;
import com.example.auto_ria.configurations.providers.SellerAuthenticationProvider;
import com.example.auto_ria.dao.*;
import com.example.auto_ria.enums.EMail;
import com.example.auto_ria.enums.ERole;
import com.example.auto_ria.enums.ETokenRole;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.mail.FMService;
import com.example.auto_ria.models.*;
import com.example.auto_ria.models.requests.*;
import com.example.auto_ria.models.responses.AuthenticationResponse;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
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

    private SellerAuthenticationProvider sellerAuthenticationManager;
    private ManagerAuthenticationProvider managerAuthenticationManager;
    private RegisterKeyDaoSQL registerKeyDaoSQL;
    private AdminAuthenticationProvider adminAuthenticationProvider;
    private CustomerAuthenticationProvider customerAuthenticationProvider;

    private PasswordEncoder passwordEncoder;
    private FMService mailer;

    public ResponseEntity<String> register(
            @Valid RegisterRequest registerRequest) {

        if (sellerDaoSQL.findSellerByEmail(registerRequest.getEmail()) != null) {
            throw new CustomException("User with this email already exists", HttpStatus.BAD_REQUEST);
        }

        SellerSQL seller = UserSQL
                .userSQLBuilder()
                .name(registerRequest.getName())
                .email(registerRequest.getEmail())
                .avatar(registerRequest.getAvatar())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .roles(List.of(ERole.SELLER, ERole.SELLER_PERSON))
                .city(registerRequest.getCity())
                .region(registerRequest.getRegion())
                .number(registerRequest.getNumber())
                .lastName(registerRequest.getLastName())
                .build();

        seller.setIsActivated(false);
        sellerDaoSQL.save(seller);

        String activateToken = jwtService.generateRegisterKey(seller.getEmail(), ETokenRole.SELLER_ACTIVATE);

        HashMap<String, Object> variables = new HashMap<>();
        variables.put("name", registerRequest.getName());
        variables.put("activation-link", "http/web/" + activateToken);

        try {
            mailer.sendEmail(registerRequest.getEmail(), EMail.WELCOME, variables);
        } catch (Exception ignore) {
        }


        return ResponseEntity.ok("Check your email for activation");
    }

    public ResponseEntity<String> codeManager(String email, String code) throws MessagingException, TemplateException, IOException {

        Map<String, Object> map = new HashMap<>();
        map.put("code", code);
        map.put("role", ERole.ADMIN.name());

        mailer.sendEmail(email, EMail.REGISTER_KEY, map);

        registerKeyDaoSQL.save(RegisterKey.builder().registerKey(code).build());

        return ResponseEntity.ok("Email sent");
    }

    public AuthenticationResponse registerManager(RegisterManagerRequest registerRequest, String key) {

        ManagerSQL manager = ManagerSQL
                .managerSQLBuilder()
                .name(registerRequest.getName())
                .email(registerRequest.getEmail())
                .avatar(registerRequest.getAvatar())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .roles(List.of(ERole.MANAGER, ERole.MANAGER_GLOBAL))
                .build();

        AuthenticationResponse authenticationResponse = jwtService.generateManagerTokenPair(manager);

        manager.setRefreshToken(authenticationResponse.getRefreshToken());

        managerDaoSQL.save(manager);

        registerKeyDaoSQL.deleteById(registerKeyDaoSQL.findByRegisterKey(key).getId());

        mailer.sendWelcomeEmail(manager.getName(), manager.getEmail());

        return AuthenticationResponse
                .builder()
                .accessToken(authenticationResponse.getAccessToken())
                .refreshToken(authenticationResponse.getRefreshToken())
                .build();
    }

    public ResponseEntity<String> codeAdmin(String email, String code) throws MessagingException, TemplateException, IOException {

        Map<String, Object> map = new HashMap<>();
        map.put("code", code);
        map.put("role", ERole.ADMIN.name());

        mailer.sendEmail(email, EMail.REGISTER_KEY, map);

        RegisterKey registerKey = registerKeyDaoSQL.save(RegisterKey.builder().registerKey(code).build());
        System.out.println("SAVED DOWN");
        System.out.println(registerKey);
        System.out.println("FIND KEY AFTER SAVED DOWN");
        System.out.println(registerKeyDaoSQL.findByRegisterKey(code));

        return ResponseEntity.ok("Email sent");
    }

    public AuthenticationResponse registerAdmin(RegisterAdminRequest registerRequest) {

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

        administrator.setRefreshToken(authenticationResponse.getRefreshToken());

        administratorDaoSQL.save(administrator);

        mailer.sendWelcomeEmail(administrator.getName(), administrator.getEmail());

        return AuthenticationResponse
                .builder()
                .accessToken(authenticationResponse.getAccessToken())
                .refreshToken(authenticationResponse.getRefreshToken())
                .build();
    }

    public ResponseEntity<String> registerCustomer(RegisterAdminRequest registerRequest) {

        CustomerSQL customerSQL = CustomerSQL
                .customerBuilder()
                .name(registerRequest.getName())
                .email(registerRequest.getEmail())
                .avatar(registerRequest.getAvatar())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .lastName(registerRequest.getLastName())
                .roles(List.of(ERole.CUSTOMER))
                .build();

        AuthenticationResponse authenticationResponse = jwtService.generateCustomerTokenPair(customerSQL);

        customerSQL.setRefreshToken(authenticationResponse.getRefreshToken());

        String activateToken = jwtService.generateRegisterKey(customerSQL.getEmail(), ETokenRole.CUSTOMER_ACTIVATE);

        HashMap<String, Object> variables = new HashMap<>();
        variables.put("name", registerRequest.getName());
        variables.put("activation-link", "http/web/" + activateToken);

        try {
            mailer.sendEmail(registerRequest.getEmail(), EMail.WELCOME, variables);
        } catch (Exception ignore) {
        }

        customerSQL.setIsActivated(false);
        customerDaoSQL.save(customerSQL);

        return ResponseEntity.ok("Check your email for activation");
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
        String access_token = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        System.out.println(access_token);
        System.out.println(refreshToken);

        user.setRefreshToken(refreshToken);
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
        AuthenticationResponse tokenPair = jwtService.generateManagerTokenPair(user);

        user.setRefreshToken(tokenPair.getRefreshToken());
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
        AuthenticationResponse tokenPair = jwtService.generateAdminTokenPair(administrator);

        administrator.setRefreshToken(tokenPair.getRefreshToken());
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
        AuthenticationResponse tokenPair = jwtService.generateCustomerTokenPair(customerSQL);

        customerSQL.setRefreshToken(tokenPair.getRefreshToken());
        customerDaoSQL.save(customerSQL);

        return AuthenticationResponse.builder().accessToken(tokenPair.getAccessToken()).refreshToken(tokenPair.getRefreshToken()).build();
    }

    public AuthenticationResponse refresh(RefreshRequest refreshRequest) {
        String refreshToken = refreshRequest.getRefreshToken();
        String username = jwtService.extractUsername(refreshToken, ETokenRole.SELLER);

        SellerSQL user = sellerDaoSQL.findSellerByEmail(username);
        String newAccessToken = null;
        String newRefreshToken = null;

        if (user.getRefreshToken().equals(refreshToken)) {
            newAccessToken = jwtService.generateToken(user);
            newRefreshToken = jwtService.generateRefreshToken(user);
            user.setRefreshToken(newRefreshToken);
            sellerDaoSQL.save(user);
        }

        return AuthenticationResponse.builder().accessToken(newAccessToken).refreshToken(newRefreshToken).build();
    }

    public AuthenticationResponse refreshManager(RefreshRequest refreshRequest) {
        String refreshToken = refreshRequest.getRefreshToken();

        String username = jwtService.extractUsername(refreshToken, ETokenRole.MANAGER);

        ManagerSQL user = managerDaoSQL.findByEmail(username);


        AuthenticationResponse tokenPair = null;

        if (user.getRefreshToken().equals(refreshToken)) {
            tokenPair = jwtService.generateManagerTokenPair(user);
            user.setRefreshToken(tokenPair.getRefreshToken());
            managerDaoSQL.save(user);
        }

        assert tokenPair != null;
        return AuthenticationResponse.builder().accessToken(tokenPair.getAccessToken()).refreshToken(tokenPair.getRefreshToken()).build();
    }

    public AuthenticationResponse refreshAdmin(RefreshRequest refreshRequest) {
        String refreshToken = refreshRequest.getRefreshToken();
        String username = jwtService.extractUsername(refreshToken, ETokenRole.ADMIN);
        AdministratorSQL administrator = administratorDaoSQL.findByEmail(username);

        AuthenticationResponse tokenPair = null;

        if (administrator.getRefreshToken().equals(refreshToken)) {
            tokenPair = jwtService.generateAdminTokenPair(administrator);
            administrator.setRefreshToken(tokenPair.getRefreshToken());
            administratorDaoSQL.save(administrator);
        }

        assert tokenPair != null;
        return AuthenticationResponse.builder().accessToken(tokenPair.getAccessToken()).refreshToken(tokenPair.getRefreshToken()).build();
    }

    public AuthenticationResponse refreshCustomer(RefreshRequest refreshRequest) {
        String refreshToken = refreshRequest.getRefreshToken();
        String username = jwtService.extractUsername(refreshToken, ETokenRole.CUSTOMER);

        CustomerSQL customerSQL = customerDaoSQL.findByEmail(username);

        AuthenticationResponse tokenPair = null;

        if (customerSQL.getRefreshToken().equals(refreshToken)) {
            tokenPair = jwtService.generateAdminTokenPair(customerSQL);
            customerSQL.setRefreshToken(tokenPair.getRefreshToken());
            customerDaoSQL.save(customerSQL);
        }

        assert tokenPair != null;
        return AuthenticationResponse.builder().accessToken(tokenPair.getAccessToken()).refreshToken(tokenPair.getRefreshToken()).build();
    }

    public String checkRegistrationKey(HttpServletRequest request, String email, ETokenRole role) {
        String authorizationHeader = request.getHeader("Register-key");
        System.out.println(authorizationHeader);
        System.out.println("hedaer ---------------------------");

        if (authorizationHeader == null) {
            throw new CustomException("Register key required", HttpStatus.FORBIDDEN);
        }

        System.out.println("db ---------------------------");
        System.out.println(registerKeyDaoSQL.findByRegisterKey(authorizationHeader));

        if (registerKeyDaoSQL.findByRegisterKey(authorizationHeader) == null) {
            throw new CustomException("Key is not valid", HttpStatus.FORBIDDEN);
        }

        if (jwtService.isTokenExprired(authorizationHeader)) {
            throw new CustomException("Key expired", HttpStatus.FORBIDDEN);
        }

//        ETokenRole keyRole = ETokenRole.valueOf(jwtService.extractIssuer(authorizationHeader));
        System.out.println(role.name());
        System.out.println(jwtService.extractIssuer(authorizationHeader));
        System.out.println(jwtService.extractIssuer(authorizationHeader).equals(role.name()));
        if (!jwtService.extractIssuer(authorizationHeader).equals(role.name())) {
            throw new CustomException("The key is not valid for creation of " + role.name().toLowerCase(), HttpStatus.FORBIDDEN);
        }

        if (!jwtService.isKeyValid(authorizationHeader, email, ETokenRole.valueOf(role.name()))) {
            throw new CustomException("Not valid key owner", HttpStatus.FORBIDDEN);
        }

        return authorizationHeader;
    }

    public ResponseEntity<AuthenticationResponse> activate(String email, ERole role) {
        if (role.equals(ERole.SELLER)) {
            SellerSQL sellerSQL = userDaoSQL.findByEmail(email);
            sellerSQL.setIsActivated(true);
            userDaoSQL.save(sellerSQL);

            String accessToken = jwtService.generateToken(sellerSQL);
            String refreshToken = jwtService.generateRefreshToken(sellerSQL);

            return ResponseEntity.ok(AuthenticationResponse
                    .builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build());

        } else if (role.equals(ERole.CUSTOMER)) {
            CustomerSQL customerSQL = customerDaoSQL.findByEmail(email);
            customerSQL.setIsActivated(true);
            customerDaoSQL.save(customerSQL);

            AuthenticationResponse authenticationResponse = jwtService.generateCustomerTokenPair(customerSQL);

            return ResponseEntity.ok(AuthenticationResponse
                    .builder()
                    .accessToken(authenticationResponse.getAccessToken())
                    .refreshToken(authenticationResponse.getRefreshToken())
                    .build());
        }

        return ResponseEntity.ok(AuthenticationResponse.builder().build());
    }

}
