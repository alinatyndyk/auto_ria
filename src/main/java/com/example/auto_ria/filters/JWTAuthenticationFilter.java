package com.example.auto_ria.filters;

import java.io.IOException;
import java.util.Enumeration;
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
            System.out.println("IN BACHEND**************************");
            String authorizationHeader = request.getHeader("Authorization");
            String authorizationParam = request.getParameter("auth");

            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                System.out.println("Header Name: " + headerName);
                System.out.println("Header Value: " + request.getHeader(headerName));
            }

            System.out.println(authorizationHeader);
            System.out.println(authorizationParam);

            if (authorizationHeader == null && authorizationParam == null) {
                filterChain.doFilter(request, response);
                return;
            }

            if (authorizationHeader != null && !authorizationHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            System.out.println("afte filter chain");

            String jwt;

            if (authorizationHeader != null) {
                jwt = authorizationHeader.substring(7);
            } else {
                jwt = authorizationParam;
            }

            String userEmail = jwtService.extractUsername(jwt);
            System.out.println(userEmail + "user email");

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    if (!isInDbAndActivated(userDetails, jwt)) {
                        throw new IllegalAccessException("Token invalid");
                    }
                    System.out.println(78);
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());

                    System.out.println(authenticationToken + "auth token");
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    System.out.println(authenticationToken + "auth token2");

                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(authenticationToken);
                    System.out.println(authenticationToken + "auth token3");
                }
            }
            System.out.println("94");
            System.out.println(request);

            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            System.out.println(e.getMessage() + "ExpiredJwtException");
            response.sendError(423);
        } catch (IllegalAccessException e) {
            System.out.println(e.getMessage() + "IllegalAccessException");
            response.getWriter().write("Jwt invalid");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
        } catch (Exception e) {
            System.out.println(e.getMessage() + "Exception");
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
