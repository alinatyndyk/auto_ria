package com.example.auto_ria.security;

import com.example.auto_ria.filters.JWTAuthenticationFilter;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfiguration {
    private JWTAuthenticationFilter jwtAuthenticationFilter;
    private AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(configurer -> configurer.disable())
                .authorizeHttpRequests(matcherRegistry ->
                        matcherRegistry
//                                .requestMatchers("/api/v1/auth/**") //todo clear
                                .requestMatchers(HttpMethod.GET, "/cars/**").permitAll()
                                .requestMatchers(HttpMethod.POST, "/cars").hasAnyAuthority("SELLER")
                                .requestMatchers(HttpMethod.DELETE, "/cars/**").hasAnyAuthority("SELLER")
                                .requestMatchers(HttpMethod.PATCH, "/cars/**").hasAnyAuthority("SELLER")
//                                .authenticated()
                                .anyRequest()
                                .permitAll()
                )
                .sessionManagement(managementConfigurer -> managementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }


}
