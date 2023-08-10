package com.example.auto_ria.services;

import com.example.auto_ria.configurations.providers.AdminAuthenticationProvider;
import com.example.auto_ria.configurations.providers.CustomerAuthenticationProvider;
import com.example.auto_ria.configurations.providers.ManagerAuthenticationProvider;
import com.example.auto_ria.configurations.providers.SellerAuthenticationProvider;
import com.example.auto_ria.dao.AdministratorDaoSQL;
import com.example.auto_ria.dao.CustomerDaoSQL;
import com.example.auto_ria.dao.ManagerDaoSQL;
import com.example.auto_ria.dao.UserDaoSQL;
import com.example.auto_ria.enums.ERole;
import com.example.auto_ria.models.*;
import com.example.auto_ria.models.requests.*;
import com.example.auto_ria.models.responses.AuthenticationResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class AuthenticationService {

    private JwtService jwtService;
    private UserDaoSQL sellerDaoSQL;
    private ManagerDaoSQL managerDaoSQL;
    private AdministratorDaoSQL administratorDaoSQL;
    private CustomerDaoSQL customerDaoSQL;

    private SellerAuthenticationProvider sellerAuthenticationManager;
    private ManagerAuthenticationProvider managerAuthenticationManager;
    private AdminAuthenticationProvider adminAuthenticationProvider;
    private CustomerAuthenticationProvider customerAuthenticationProvider;

    private PasswordEncoder passwordEncoder;

    public AuthenticationResponse register(RegisterRequest registerRequest) {

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

        String accessToken = jwtService.generateToken(seller);
        String refreshToken = jwtService.generateRefreshToken(seller);

        assert seller != null;
        seller.setRefreshToken(refreshToken);

        sellerDaoSQL.save(seller);

        return AuthenticationResponse
                .builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthenticationResponse registerManager(RegisterManagerRequest registerRequest) {

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

        return AuthenticationResponse
                .builder()
                .accessToken(authenticationResponse.getAccessToken())
                .refreshToken(authenticationResponse.getRefreshToken())
                .build();
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

        return AuthenticationResponse
                .builder()
                .accessToken(authenticationResponse.getAccessToken())
                .refreshToken(authenticationResponse.getRefreshToken())
                .build();
    }

    public AuthenticationResponse registerCustomer(RegisterAdminRequest registerRequest) {

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

        customerDaoSQL.save(customerSQL);

        return AuthenticationResponse
                .builder()
                .accessToken(authenticationResponse.getAccessToken())
                .refreshToken(authenticationResponse.getRefreshToken())
                .build();
    }


    public AuthenticationResponse login(LoginRequest loginRequest) {
        SellerSQL user = sellerDaoSQL.findSellerByEmail(loginRequest.getEmail());
        sellerAuthenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword(),
                        user.getAuthorities()
                )
        );
        String access_token = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        user.setRefreshToken(refreshToken);
        sellerDaoSQL.save(user);

        return AuthenticationResponse.builder().accessToken(access_token).refreshToken(refreshToken).build();
    }

    public AuthenticationResponse loginManager(LoginRequest loginRequest) {
        ManagerSQL user = managerDaoSQL.findByEmail(loginRequest.getEmail());

        managerAuthenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword(),
                        user.getAuthorities()
                )
        );
        AuthenticationResponse tokenPair = jwtService.generateManagerTokenPair(user);

        user.setRefreshToken(tokenPair.getRefreshToken());
        managerDaoSQL.save(user);

        return AuthenticationResponse.builder().accessToken(tokenPair.getAccessToken()).refreshToken(tokenPair.getRefreshToken()).build();
    }

    public AuthenticationResponse loginAdmin(LoginRequest loginRequest) {
        AdministratorSQL administrator = administratorDaoSQL.findByEmail(loginRequest.getEmail());

        adminAuthenticationProvider.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword(),
                        administrator.getAuthorities()
                )
        );
        AuthenticationResponse tokenPair = jwtService.generateAdminTokenPair(administrator);

        administrator.setRefreshToken(tokenPair.getRefreshToken());
        administratorDaoSQL.save(administrator);

        return AuthenticationResponse.builder().accessToken(tokenPair.getAccessToken()).refreshToken(tokenPair.getRefreshToken()).build();
    }

    public AuthenticationResponse loginCustomer(LoginRequest loginRequest) {
        CustomerSQL customerSQL = customerDaoSQL.findByEmail(loginRequest.getEmail());

        customerAuthenticationProvider.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword(),
                        customerSQL.getAuthorities()
                )
        );
        AuthenticationResponse tokenPair = jwtService.generateCustomerTokenPair(customerSQL);

        customerSQL.setRefreshToken(tokenPair.getRefreshToken());
        customerDaoSQL.save(customerSQL);

        return AuthenticationResponse.builder().accessToken(tokenPair.getAccessToken()).refreshToken(tokenPair.getRefreshToken()).build();
    }

    public AuthenticationResponse refresh(RefreshRequest refreshRequest) {
        String refreshToken = refreshRequest.getRefreshToken();
        String username = jwtService.extractUsername(refreshToken, ERole.SELLER);

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

        String username = jwtService.extractUsername(refreshToken, ERole.MANAGER); //extract from manager

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
        String username = jwtService.extractUsername(refreshToken, ERole.ADMIN);

        AdministratorSQL administrator = administratorDaoSQL.findByEmail(username);

        AuthenticationResponse tokenPair = null;

        if (administrator.getRefreshToken().equals(refreshToken)) {
            tokenPair = jwtService.generateAdminTokenPair(administrator);
            administrator.setRefreshToken(tokenPair.getRefreshToken());
            administratorDaoSQL.save(administrator);
        }

        System.out.println(tokenPair);

        assert tokenPair != null;
        return AuthenticationResponse.builder().accessToken(tokenPair.getAccessToken()).refreshToken(tokenPair.getRefreshToken()).build();
    }

    public AuthenticationResponse refreshCustomer(RefreshRequest refreshRequest) {
        String refreshToken = refreshRequest.getRefreshToken();
        String username = jwtService.extractUsername(refreshToken, ERole.CUSTOMER);

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


}
