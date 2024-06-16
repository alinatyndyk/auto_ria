package com.example.auto_ria.configurations.providers;

import java.util.Collection;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.auto_ria.dao.user.UserDaoSQL;
import com.example.auto_ria.models.user.UserSQL;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class UserAuthenticationProvider implements AuthenticationProvider {

    private UserDaoSQL userDaoSQL;
    private PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();
        Collection<? extends GrantedAuthority> permissions = authentication.getAuthorities();

        UserSQL user = userDaoSQL.findByEmail(username);

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        return new UsernamePasswordAuthenticationToken(username, password, permissions);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
