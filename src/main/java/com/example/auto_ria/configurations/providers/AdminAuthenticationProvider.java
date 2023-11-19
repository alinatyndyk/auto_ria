package com.example.auto_ria.configurations.providers;

import com.example.auto_ria.dao.user.AdministratorDaoSQL;
import com.example.auto_ria.models.user.AdministratorSQL;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
@AllArgsConstructor
public class AdminAuthenticationProvider implements AuthenticationProvider {

    private AdministratorDaoSQL administratorDaoSQL;
    private PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();
        Collection<? extends GrantedAuthority> permissions = authentication.getAuthorities();

        AdministratorSQL administrator = administratorDaoSQL.findByEmail(username);

        if (!passwordEncoder.matches(password, administrator.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        return new UsernamePasswordAuthenticationToken(username, password, permissions);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
