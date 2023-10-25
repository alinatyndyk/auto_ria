package com.example.auto_ria.filters;

import com.example.auto_ria.dao.authDao.AdminAuthDaoSQL;
import com.example.auto_ria.dao.authDao.CustomerAuthDaoSQL;
import com.example.auto_ria.dao.authDao.ManagerAuthDaoSQL;
import com.example.auto_ria.dao.authDao.SellerAuthDaoSQL;
import com.example.auto_ria.enums.ERole;
import com.example.auto_ria.services.JwtService;
import com.example.auto_ria.services.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@AllArgsConstructor
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    private JwtService jwtService;

    private UserDetailsServiceImpl userDetailsService;

    private AdminAuthDaoSQL adminAuthDaoSQL;
    private ManagerAuthDaoSQL managerAuthDaoSQL;
    private SellerAuthDaoSQL sellerAuthDaoSQL;
    private CustomerAuthDaoSQL customerAuthDaoSQL;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authorizationHeader.substring(7);

        String userEmail = jwtService.extractUsername(jwt);

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);


            if (
                    jwtService.isTokenValid(jwt, userDetails)
                            &&
                            isInDb(userDetails, jwt)
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
            } else {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.getWriter().write("Token invalid");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean isInDb(UserDetails userDetails, String jwt) {
        if (userDetails.getAuthorities().contains(new SimpleGrantedAuthority(ERole.ADMIN.name()))) {
            return adminAuthDaoSQL.findByAccessToken(jwt) != null;
        } else if (userDetails.getAuthorities().contains(new SimpleGrantedAuthority(ERole.MANAGER.name()))) {
            return managerAuthDaoSQL.findByAccessToken(jwt) != null;
        } else if (userDetails.getAuthorities().contains(new SimpleGrantedAuthority(ERole.CUSTOMER.name()))) {
            return customerAuthDaoSQL.findByAccessToken(jwt) != null;
        } else if (userDetails.getAuthorities().contains(new SimpleGrantedAuthority(ERole.SELLER.name()))) {
            return sellerAuthDaoSQL.findByAccessToken(jwt) != null;
        }
        return false;
    }


}
