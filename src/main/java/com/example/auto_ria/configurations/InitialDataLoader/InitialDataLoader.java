package com.example.auto_ria.configurations.InitialDataLoader;

import com.example.auto_ria.dao.AdministratorDaoSQL;
import com.example.auto_ria.models.requests.RegisterAdminRequest;
import com.example.auto_ria.services.AuthenticationService;
import lombok.AllArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@AllArgsConstructor
public class InitialDataLoader {

    private Environment environment;
    private AdministratorDaoSQL administratorDaoSQL;
    private AuthenticationService authenticationService;

    @PostConstruct
    public void loadInitialData() {
        if (administratorDaoSQL.count() == 0) {

            RegisterAdminRequest request = RegisterAdminRequest.builder()
                    .name(environment.getProperty("initial.admin.name"))
                    .email(environment.getProperty("initial.admin.email"))
                    .password(environment.getProperty("initial.admin.pass"))
                    .build();

            authenticationService.registerAdmin(request);
        }
    }
}
