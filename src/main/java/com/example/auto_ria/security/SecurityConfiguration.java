package com.example.auto_ria.security;

import com.example.auto_ria.configurations.providers.AdminAuthenticationProvider;
import com.example.auto_ria.configurations.providers.ManagerAuthenticationProvider;
import com.example.auto_ria.configurations.providers.SellerAuthenticationProvider;
import com.example.auto_ria.filters.JWTAuthenticationFilter;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfiguration {
    private JWTAuthenticationFilter jwtAuthenticationFilter;
    private SellerAuthenticationProvider sellerAuthenticationProvider;
    private ManagerAuthenticationProvider managerAuthenticationProvider;
    private AdminAuthenticationProvider adminAuthenticationProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(matcherRegistry ->
                                matcherRegistry
                                        .requestMatchers("/api/v1/auth/**").permitAll()
                                        .requestMatchers(HttpMethod.POST, "/cars").hasAnyAuthority("SELLER", "ADMIN")
                                        .requestMatchers(HttpMethod.DELETE, "/cars/**").hasAnyAuthority("SELLER", "ADMIN", "MANAGER")
                                        .requestMatchers(HttpMethod.PATCH, "/cars/**").hasAnyAuthority("SELLER", "ADMIN")

                                        .requestMatchers(HttpMethod.DELETE, "/sellers/**").hasAnyAuthority("SELLER", "ADMIN", "MANAGER")
                                        .requestMatchers(HttpMethod.PATCH, "sellers/**").hasAnyAuthority("SELLER", "ADMIN")

                                        .requestMatchers(HttpMethod.POST, "/managers").hasAnyAuthority("ADMIN") //todo managers service and controller
                                        .requestMatchers(HttpMethod.DELETE, "/managers/**").hasAnyAuthority("SELLER_COMPANY", "ADMIN")
                                        .requestMatchers(HttpMethod.PATCH, "/managers/**").hasAnyAuthority("SELLER_COMPANY", "ADMIN")

//                                .authenticated()
                                        .anyRequest()
                                        .permitAll()
                )
                .sessionManagement(managementConfigurer -> managementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(sellerAuthenticationProvider)
                .authenticationProvider(managerAuthenticationProvider)
                .authenticationProvider(adminAuthenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }


}
