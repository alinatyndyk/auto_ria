package com.example.auto_ria.security;

import com.example.auto_ria.configurations.providers.AdminAuthenticationProvider;
import com.example.auto_ria.configurations.providers.CustomerAuthenticationProvider;
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
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfiguration {
    private JWTAuthenticationFilter jwtAuthenticationFilter;
    private SellerAuthenticationProvider sellerAuthenticationProvider;
    private ManagerAuthenticationProvider managerAuthenticationProvider;
    private AdminAuthenticationProvider adminAuthenticationProvider;
    private CustomerAuthenticationProvider customerAuthenticationProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(matcherRegistry ->
                                matcherRegistry
//                                        .requestMatchers("/api/v1/auth/**").permitAll()
                                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/code-admin").hasAnyAuthority("ADMIN")
                                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/code-manager").hasAnyAuthority("ADMIN")
                                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/sign-out").hasAnyAuthority("ADMIN", "MANAGER", "SELLER", "CUSTOMER")
                                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/change-password").hasAnyAuthority("ADMIN", "MANAGER", "SELLER", "CUSTOMER")


                                        .requestMatchers(HttpMethod.GET, "/cars").hasAnyAuthority( "ADMIN")
                                        .requestMatchers(HttpMethod.POST, "/cars").hasAnyAuthority("SELLER", "ADMIN")
                                        .requestMatchers(HttpMethod.POST, "/cars/activate/{id}").hasAnyAuthority("MANAGER", "ADMIN")
                                        .requestMatchers(HttpMethod.POST, "/cars/ban/{id}").hasAnyAuthority("MANAGER", "ADMIN")
                                        .requestMatchers(HttpMethod.GET, "/cars/statistics/{id}").hasAnyAuthority("SELLER", "ADMIN", "MANAGER")
                                        .requestMatchers(HttpMethod.GET, "/cars/middle/{id}").hasAnyAuthority("SELLER", "ADMIN", "MANAGER")
                                        .requestMatchers(HttpMethod.GET, "/cars/buy-premium").hasAnyAuthority("SELLER")
                                        .requestMatchers(HttpMethod.DELETE, "/cars/**").hasAnyAuthority("SELLER", "ADMIN", "MANAGER")
                                        .requestMatchers(HttpMethod.PATCH, "/cars/**").hasAnyAuthority("SELLER", "ADMIN")

                                        .requestMatchers(HttpMethod.DELETE, "/sellers/**").hasAnyAuthority("SELLER", "ADMIN", "MANAGER")
                                        .requestMatchers(HttpMethod.PATCH, "sellers/**").hasAnyAuthority("SELLER", "ADMIN")
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
                                        .requestMatchers(HttpMethod.DELETE, "/customers/**").hasAnyAuthority("CUSTOMER", "MANAGER", "ADMIN")
                                        .requestMatchers(HttpMethod.PATCH, "/customers/**").hasAnyAuthority("ADMIN", "CUSTOMER")

//                                .authenticated()
                                        .anyRequest()
                                        .permitAll()
                )
                .sessionManagement(managementConfigurer -> managementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(sellerAuthenticationProvider)
                .authenticationProvider(managerAuthenticationProvider)
                .authenticationProvider(adminAuthenticationProvider)
                .authenticationProvider(customerAuthenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }


}
