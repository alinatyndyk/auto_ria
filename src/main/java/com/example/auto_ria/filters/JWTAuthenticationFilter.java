package com.example.auto_ria.filters;

import com.example.auto_ria.dao.auth.AdminAuthDaoSQL;
import com.example.auto_ria.dao.auth.CustomerAuthDaoSQL;
import com.example.auto_ria.dao.auth.ManagerAuthDaoSQL;
import com.example.auto_ria.dao.auth.SellerAuthDaoSQL;
import com.example.auto_ria.enums.ERole;
import com.example.auto_ria.services.auth.JwtService;
import com.example.auto_ria.services.auth.UserDetailsServiceImpl;
import com.example.auto_ria.services.user.AdministratorServiceMySQL;
import com.example.auto_ria.services.user.CustomersServiceMySQL;
import com.example.auto_ria.services.user.ManagerServiceMySQL;
import com.example.auto_ria.services.user.UsersServiceMySQLImpl;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
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

    private AdministratorServiceMySQL administratorServiceMySQL;
    private ManagerServiceMySQL managerServiceMySQL;
    private UsersServiceMySQLImpl usersServiceMySQL;
    private CustomersServiceMySQL customersServiceMySQL;

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


            System.out.println(jwt);
            String userEmail = jwtService.extractUsername(jwt);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                if (
                        jwtService.isTokenValid(jwt, userDetails)
                ) {
                    if (!isInDbAndActivated(userDetails, jwt)) {
                        throw new IllegalAccessException("Token invalid");
                    }

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
        if (userDetails.getAuthorities().contains(new SimpleGrantedAuthority(ERole.ADMIN.name()))
                && administratorServiceMySQL.getByEmail(userDetails.getUsername()).getIsActivated().equals(true)) {
            return adminAuthDaoSQL.findByAccessToken(jwt) != null;
        } else if (userDetails.getAuthorities().contains(new SimpleGrantedAuthority(ERole.MANAGER.name()))
                && managerServiceMySQL.getByEmail(userDetails.getUsername()).getIsActivated().equals(true)) {
            return managerAuthDaoSQL.findByAccessToken(jwt) != null;
        } else if (userDetails.getAuthorities().contains(new SimpleGrantedAuthority(ERole.CUSTOMER.name()))
                && customersServiceMySQL.getByEmail(userDetails.getUsername()).getIsActivated().equals(true)) {
            return customerAuthDaoSQL.findByAccessToken(jwt) != null;
        } else if (userDetails.getAuthorities().contains(new SimpleGrantedAuthority(ERole.SELLER.name()))
                && usersServiceMySQL.getByEmail(userDetails.getUsername()).getIsActivated().equals(true)) {
            return sellerAuthDaoSQL.findByAccessToken(jwt) != null;
        }
        return false;
    }


}
