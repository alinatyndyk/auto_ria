package com.example.auto_ria.services;

import com.example.auto_ria.configurations.providers.AdminAuthenticationProvider;
import com.example.auto_ria.configurations.providers.ManagerAuthenticationProvider;
import com.example.auto_ria.configurations.providers.SellerAuthenticationProvider;
import com.example.auto_ria.dao.AdministratorDaoSQL;
import com.example.auto_ria.dao.ManagerDaoSQL;
import com.example.auto_ria.dao.UserDaoSQL;
import com.example.auto_ria.enums.ERole;
import com.example.auto_ria.models.Administrator;
import com.example.auto_ria.models.Manager;
import com.example.auto_ria.models.SellerSQL;
import com.example.auto_ria.models.UserSQL;
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
    //    private UserRepoMD userRepoMD;
    // todo add mongo
    private SellerAuthenticationProvider sellerAuthenticationManager;
    private ManagerAuthenticationProvider managerAuthenticationManager;
    private AdminAuthenticationProvider adminAuthenticationProvider;

    private PasswordEncoder passwordEncoder;

    public AuthenticationResponse register(RegisterRequest registerRequest) {

        UserSQL seller = UserSQL
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

        Manager manager = Manager
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

        Administrator administrator = Administrator
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
        Manager user = managerDaoSQL.findByEmail(loginRequest.getEmail());

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
        Administrator administrator = administratorDaoSQL.findByEmail(loginRequest.getEmail());

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

    public AuthenticationResponse refresh(RefreshRequest refreshRequest) {
        String refreshToken = refreshRequest.getRefreshToken();
        String username = jwtService.extractUsername(refreshToken);

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
        String username = jwtService.extractUsername(refreshToken);

        Manager user = managerDaoSQL.findByEmail(username);

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
        String username = jwtService.extractUsername(refreshToken);

        Administrator administrator = administratorDaoSQL.findByEmail(username);

        AuthenticationResponse tokenPair = null;

        if (administrator.getRefreshToken().equals(refreshToken)) {
            tokenPair = jwtService.generateAdminTokenPair(administrator);
            administrator.setRefreshToken(tokenPair.getRefreshToken());
            administratorDaoSQL.save(administrator);
        }

        assert tokenPair != null;
        return AuthenticationResponse.builder().accessToken(tokenPair.getAccessToken()).refreshToken(tokenPair.getRefreshToken()).build();
    }


}
