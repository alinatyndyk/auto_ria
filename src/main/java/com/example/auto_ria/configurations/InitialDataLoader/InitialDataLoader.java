package com.example.auto_ria.configurations.InitialDataLoader;

import com.example.auto_ria.dao.user.AdministratorDaoSQL;
import com.example.auto_ria.exceptions.CustomException;
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
        try {
            if (administratorDaoSQL.count() == 0) {

                RegisterAdminRequest request = RegisterAdminRequest.builder()
                        .name(environment.getProperty("initial.admin.name"))
                        .lastName(environment.getProperty("initial.admin.lastName"))
                        .email(environment.getProperty("initial.admin.email"))
                        .password(environment.getProperty("initial.admin.pass"))
                        .build();

                authenticationService.registerAdmin(request);
            }
        } catch (CustomException e) {
            throw new CustomException("Error while loading initial data" + e.getMessage(), e.getStatus());
        }
    }
}
