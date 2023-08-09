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
            String userEmail = jwtService.extractUsername(jwt);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
                System.out.println(userDetails);

                if (
                        jwtService.isTokenValid(jwt, userDetails)
//                        && // todo check if refresh
//                        !jwt.equals(userDaoSQL.findSellerByEmail(userEmail).getRefreshToken())
                ) {
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(authenticationToken);
                }
            }
        } catch (ExpiredJwtException e) {
            response.setHeader(HttpHeaders.EXPIRES, "dead");
            response.resetBuffer();
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

            ErrorResponse errorResponse = ErrorResponse  //todo custom error
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
