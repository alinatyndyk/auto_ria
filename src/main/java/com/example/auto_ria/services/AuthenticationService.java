package com.example.auto_ria.services;

import com.example.auto_ria.dao.ManagerDaoSQL;
import com.example.auto_ria.dao.UserDaoSQL;
import com.example.auto_ria.enums.ERole;
import com.example.auto_ria.models.Manager;
import com.example.auto_ria.models.SellerSQL;
import com.example.auto_ria.models.UserSQL;
import com.example.auto_ria.models.requests.LoginRequest;
import com.example.auto_ria.models.requests.RefreshRequest;
import com.example.auto_ria.models.requests.RegisterManagerRequest;
import com.example.auto_ria.models.requests.RegisterRequest;
import com.example.auto_ria.models.responses.AuthenticationResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
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
    //    private UserRepoMD userRepoMD;
    // todo add mongo
    private AuthenticationManager authenticationManager;
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

        System.out.println(70);
        Manager manager = Manager
                .managerSQLBuilder()
                .name(registerRequest.getName())
                .email(registerRequest.getEmail())
                .avatar(registerRequest.getAvatar())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .roles(List.of(ERole.MANAGER, ERole.MANAGER_GLOBAL))
                .build();

//        Map<String, String> extraClaims = new HashMap<>();
//        extraClaims.put("permission", ERole.MANAGER_GLOBAL.name());  works

        AuthenticationResponse authenticationResponse = jwtService.generateManagerTokenPair(manager);

        manager.setRefreshToken(authenticationResponse.getRefreshToken());

        managerDaoSQL.save(manager);

        return AuthenticationResponse
                .builder()
                .accessToken(authenticationResponse.getAccessToken())
                .refreshToken(authenticationResponse.getRefreshToken())
                .build();
    }

    public AuthenticationResponse login(LoginRequest loginRequest) {
        System.out.println("93");
        System.out.println(loginRequest);
        System.out.println("login request");
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );
        System.out.println("100");
        SellerSQL user = sellerDaoSQL.findSellerByEmail(loginRequest.getEmail());
        System.out.println("102");
        String access_token = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        System.out.println("105");

        user.setRefreshToken(refreshToken);
        sellerDaoSQL.save(user);
        System.out.println("109");

        return AuthenticationResponse.builder().accessToken(access_token).refreshToken(refreshToken).build();
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


}
