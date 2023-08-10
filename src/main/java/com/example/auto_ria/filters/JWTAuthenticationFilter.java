package com.example.auto_ria.filters;

import com.example.auto_ria.models.responses.ErrorResponse;
import com.example.auto_ria.services.JwtService;
import com.example.auto_ria.services.UserDetailsServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@AllArgsConstructor
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    private JwtService jwtService;
    private UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = authorizationHeader.substring(7);
            System.out.println("userEmail filter1");
            String userEmail = jwtService.extractUsername(jwt);
            System.out.println(userEmail);
            System.out.println("userEmail filter");

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                System.out.println("first if");
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
                System.out.println(userDetails);
                System.out.println("user details");

                if (true
//                        jwtService.isTokenValid(jwt, userDetails)
//                        && // todo check if refresh
//                        !jwt.equals(userDaoSQL.findSellerByEmail(userEmail).getRefreshToken())
                ) {
                System.out.println("second if");
                    System.out.println(userDetails);
                    System.out.println(userDetails.getAuthorities());
                    System.out.println(userDetails.getUsername());
                    System.out.println(userDetails.getPassword());
                    System.out.println(userDetails);
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    System.out.println("SET DETAILS");

                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(authenticationToken);
                }
                System.out.println("CONTEXT HOLDER");
            }
        } catch (ExpiredJwtException e) {
            response.setHeader(HttpHeaders.EXPIRES, "dead");
            response.resetBuffer();
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

            ErrorResponse errorResponse = ErrorResponse  //todo todo custom error
                    .builder()
                    .statusCode(403)
                    .message("jwt expired")
                    .build();
            response.getOutputStream().write(new ObjectMapper().writeValueAsBytes(errorResponse));
            return;
        } catch (UsernameNotFoundException e) {
            throw new RuntimeException(e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
