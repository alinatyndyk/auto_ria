package com.example.auto_ria.configurations.InitialDataLoader;

import com.example.auto_ria.dao.AdministratorDaoSQL;
import com.example.auto_ria.models.requests.RegisterAdminRequest;
import com.example.auto_ria.services.AuthenticationService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@AllArgsConstructor
public class InitialDataLoader {

    private AdministratorDaoSQL administratorDaoSQL;
    private AuthenticationService authenticationService;

    @PostConstruct
    public void loadInitialData() {
        if (administratorDaoSQL.count() == 0) {
            RegisterAdminRequest request = RegisterAdminRequest.builder()
                    .name("Initial Admin")
                    .email("alinatyndyk777@gmail.com") //todo to env
                    .password("password123")
                    .build();
            authenticationService.registerAdmin(request);
        }
    }
}
