package com.example.auto_ria.filters;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.auto_ria.dao.auth.UserAuthDaoSQL;
import com.example.auto_ria.services.auth.JwtService;
import com.example.auto_ria.services.auth.UserDetailsServiceImpl;
import com.example.auto_ria.services.user.UsersServiceMySQLImpl;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    private JwtService jwtService;
    private UserDetailsServiceImpl userDetailsService;
    private UserAuthDaoSQL userAuthDaoSQL;
    private UsersServiceMySQLImpl usersServiceMySQL;

    @SuppressWarnings("null")
    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain) throws IOException {
        try {

            String authorizationHeader = request.getHeader("Authorization");
            String authorizationParam = request.getParameter("auth");

            if (authorizationHeader == null && authorizationParam == null) {
                filterChain.doFilter(request, response);
                return;
            }

            if (authorizationHeader != null && !authorizationHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            String jwt;

            if (authorizationHeader != null) {
                jwt = authorizationHeader.substring(7);
            } else {
                jwt = authorizationParam;
            }

            String userEmail = jwtService.extractUsername(jwt);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
                System.out.println(userDetails + "********************");
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    System.out.println(jwtService.isTokenValid(jwt, userDetails) + "********************");
                    System.out.println(!isInDbAndActivated(userDetails, jwt) + "********************");
                    if (!isInDbAndActivated(userDetails, jwt)) {
                        throw new IllegalAccessException("Token invalid");
                    }
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());

                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(authenticationToken);
                }
            }

            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            response.sendError(423);
        } catch (IllegalAccessException e) {
            response.getWriter().write("Jwt invalid");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
        } catch (Exception e) {
            response.getWriter().write(e.getMessage());
            response.setStatus(HttpStatus.EXPECTATION_FAILED.value());
        }
    }

    private boolean isInDbAndActivated(UserDetails userDetails, String jwt) {
        if (hasAllowedRole(userDetails)
                && usersServiceMySQL.getByEmail(userDetails.getUsername()).getIsActivated().equals(true)) {
            return userAuthDaoSQL.findByAccessToken(jwt) != null;
        }
        return false;
    }

    public static boolean hasAllowedRole(UserDetails userDetails) {
        Set<String> allowedRoles = new HashSet<>();
        allowedRoles.add("USER");
        allowedRoles.add("ADMIN");
        allowedRoles.add("MANAGER");

        for (GrantedAuthority authority : userDetails.getAuthorities()) {
            if (allowedRoles.contains(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }

}
