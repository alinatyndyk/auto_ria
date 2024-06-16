package com.example.auto_ria.configurations;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.example.auto_ria.configurations.providers.AdminAuthenticationProvider;
import com.example.auto_ria.configurations.providers.ManagerAuthenticationProvider;
import com.example.auto_ria.configurations.providers.UserAuthenticationProvider;
import com.example.auto_ria.dao.user.AdministratorDaoSQL;
import com.example.auto_ria.dao.user.ManagerDaoSQL;
import com.example.auto_ria.dao.user.UserDaoSQL;

import lombok.AllArgsConstructor;

@Configuration
@AllArgsConstructor
public class ApplicationConfiguration {

    private UserDaoSQL userDAO;
    private ManagerDaoSQL managerDaoSQL;
    private AdministratorDaoSQL administratorDaoSQL;

    private ManagerAuthenticationProvider managerAuthenticationProvider;
    private UserAuthenticationProvider userAuthenticationProvider;
    private AdminAuthenticationProvider adminAuthenticationProvider;

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userDAO.findUserByEmail(username);
    }

    @Bean
    public UserDetailsService managerDetailsService() {
        return username -> managerDaoSQL.findByEmail(username);
    }

    @Bean
    public UserDetailsService adminDetailsService() {
        return username -> administratorDaoSQL.findByEmail(username);
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(
                List.of(userAuthenticationProvider,
                        managerAuthenticationProvider,
                        adminAuthenticationProvider));
    }
}
