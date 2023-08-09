package com.example.auto_ria.configurations;

import com.example.auto_ria.configurations.providers.AdminAuthenticationProvider;
import com.example.auto_ria.configurations.providers.ManagerAuthenticationProvider;
import com.example.auto_ria.configurations.providers.SellerAuthenticationProvider;
import com.example.auto_ria.dao.AdministratorDaoSQL;
import com.example.auto_ria.dao.ManagerDaoSQL;
import com.example.auto_ria.dao.UserDaoSQL;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

@Configuration
@AllArgsConstructor
public class ApplicationConfiguration {

    private UserDaoSQL userDAO;
    private ManagerDaoSQL managerDaoSQL;
    private AdministratorDaoSQL administratorDaoSQL;

    private SellerAuthenticationProvider sellerAuthenticationProvider;
    private ManagerAuthenticationProvider managerAuthenticationProvider;
    private AdminAuthenticationProvider adminAuthenticationProvider;


    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userDAO.findSellerByEmail(username);
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
        return new ProviderManager(List.of(sellerAuthenticationProvider, managerAuthenticationProvider, adminAuthenticationProvider));
    }
}
