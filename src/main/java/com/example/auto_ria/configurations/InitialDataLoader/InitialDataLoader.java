package com.example.auto_ria.configurations.InitialDataLoader;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.auto_ria.dao.user.UserDaoSQL;
import com.example.auto_ria.enums.ERole;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.models.user.UserSQL;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class InitialDataLoader {

    private Environment environment;
    private PasswordEncoder passwordEncoder;
    private UserDaoSQL userDaoSQL;

    @PostConstruct
    public void loadInitialData() {
        try {
            if (userDaoSQL.count() == 0) {

                UserSQL user = UserSQL.userBuilder()
                        .name(environment.getProperty("initial.admin.name"))
                        .lastName(environment.getProperty("initial.admin.lastName"))
                        .email(environment.getProperty("initial.admin.email"))
                        .password(passwordEncoder.encode(environment.getProperty("initial.admin.pass")))
                        .roles(List.of(ERole.USER, ERole.ADMIN, ERole.ADMIN_GLOBAL))
                        .avatar(null)
                        .city(environment.getProperty("initial.admin.city"))
                        .region(environment.getProperty("initial.admin.region"))
                        .number(environment.getProperty("initial.admin.number"))
                        .build();

                userDaoSQL.save(user);

            }
        } catch (CustomException e) {
            throw new CustomException("Error while loading initial data" + e.getMessage(), e.getStatus());
        }
    }
}
