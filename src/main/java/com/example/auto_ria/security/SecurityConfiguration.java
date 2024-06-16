package com.example.auto_ria.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.auto_ria.configurations.providers.AdminAuthenticationProvider;
import com.example.auto_ria.configurations.providers.ManagerAuthenticationProvider;
import com.example.auto_ria.configurations.providers.UserAuthenticationProvider;
import com.example.auto_ria.filters.JWTAuthenticationFilter;

import lombok.AllArgsConstructor;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfiguration {
    private JWTAuthenticationFilter jwtAuthenticationFilter;
    private UserAuthenticationProvider userAuthenticationProvider;
    private ManagerAuthenticationProvider managerAuthenticationProvider;
    private AdminAuthenticationProvider adminAuthenticationProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(matcherRegistry -> matcherRegistry
                        // .requestMatchers(HttpMethod.POST, "/api/v1/auth/code-admin").hasAnyAuthority("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/code-manager").hasAnyAuthority("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/sign-out")
                        .hasAnyAuthority("ADMIN", "MANAGER", "USER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/change-passwords")
                        .hasAnyAuthority("ADMIN", "MANAGER", "USER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/sign-out")
                        .hasAnyAuthority("ADMIN", "MANAGER", "USER")

                        .requestMatchers(HttpMethod.GET, "/cars").hasAnyAuthority("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/cars").hasAnyAuthority("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/cars/activate/{id}").hasAnyAuthority("MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/cars/ban/{id}").hasAnyAuthority("MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/cars/statistics/{id}")
                        .hasAnyAuthority("USER", "ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.GET, "/cars/middle/{id}")
                        .hasAnyAuthority("USER", "ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/cars/**").hasAnyAuthority("USER", "ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.PATCH, "/cars/**").hasAnyAuthority("USER", "ADMIN")

                        .requestMatchers(HttpMethod.GET, "/payments/buy-premium").hasAnyAuthority("USER")
                        .requestMatchers(HttpMethod.POST, "/payments/add-payment-source").hasAnyAuthority("USER")

                        .requestMatchers(HttpMethod.DELETE, "/sellers/**").hasAnyAuthority("USER", "ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.PATCH, "sellers/**").hasAnyAuthority("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "sellers").hasAnyAuthority("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/managers/**").hasAnyAuthority("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/managers").hasAnyAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/managers/**").hasAnyAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/managers/**").hasAnyAuthority("ADMIN", "MANAGER")

                        .requestMatchers(HttpMethod.GET, "/administrators/**").hasAnyAuthority("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/administrators").hasAnyAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/administrators/**").hasAnyAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/administrators/**").hasAnyAuthority("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/customers/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/customers").hasAnyAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/customers/**")
                        .hasAnyAuthority("USER", "MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/customers/**").hasAnyAuthority("ADMIN", "USER")

                        .requestMatchers(HttpMethod.PATCH, "/chats/**")
                        .hasAnyAuthority("USER", "MANAGER", "ADMIN")
                        .requestMatchers("/chat/**").hasAnyAuthority("USER", "MANAGER", "ADMIN")

                        .anyRequest()
                        .permitAll())
                .sessionManagement(managementConfigurer -> managementConfigurer
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(userAuthenticationProvider)
                .authenticationProvider(managerAuthenticationProvider)
                .authenticationProvider(adminAuthenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

}
