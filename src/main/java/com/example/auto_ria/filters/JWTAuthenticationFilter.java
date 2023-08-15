package com.example.auto_ria.filters;

import com.example.auto_ria.dao.AdministratorDaoSQL;
import com.example.auto_ria.dao.CustomerDaoSQL;
import com.example.auto_ria.dao.ManagerDaoSQL;
import com.example.auto_ria.dao.UserDaoSQL;
import com.example.auto_ria.enums.ERole;
import com.example.auto_ria.exceptions.CustomException;
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
    private UserDaoSQL userDaoSQL;
    private ManagerDaoSQL managerDaoSQL;
    private CustomerDaoSQL customerDaoSQL;
    private AdministratorDaoSQL administratorDaoSQL;
    private UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = authorizationHeader.substring(7);
            String userEmail = jwtService.extractUsername(jwt);
            System.out.println(userEmail);
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                System.out.println("first uf1");
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                System.out.println(userDetails);
                System.out.println("first uf");
                if (
                        jwtService.isTokenValid(jwt, userDetails)
                                &&
                !isRefresh(userDetails, jwt, userEmail)
                ) {
                    System.out.println("secondif");
                    System.out.println(userDetails);
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
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new CustomException(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }

        filterChain.doFilter(request, response);
    }

    private boolean isRefresh(UserDetails userDetails, String jwt, String userEmail) {
        if (userDetails.getAuthorities().contains(new SimpleGrantedAuthority(ERole.SELLER.name()))) {
            return jwt.equals(userDaoSQL.findSellerByEmail(userEmail).getRefreshToken());
        } else if (userDetails.getAuthorities().contains(new SimpleGrantedAuthority(ERole.MANAGER.name()))) {
            return jwt.equals(managerDaoSQL.findByEmail(userEmail).getRefreshToken());
        } else if (userDetails.getAuthorities().contains(new SimpleGrantedAuthority(ERole.CUSTOMER.name()))) {
            return jwt.equals(customerDaoSQL.findByEmail(userEmail).getRefreshToken());
        } else if (userDetails.getAuthorities().contains(new SimpleGrantedAuthority(ERole.ADMIN.name()))) {
            return jwt.equals(administratorDaoSQL.findByEmail(userEmail).getRefreshToken());
        }
        return false;
    }


}
