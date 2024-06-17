package com.example.auto_ria.filters;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.auto_ria.dao.auth.AdminAuthDaoSQL;
import com.example.auto_ria.dao.auth.ManagerAuthDaoSQL;
import com.example.auto_ria.dao.auth.UserAuthDaoSQL;
import com.example.auto_ria.enums.ERole;
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

    private AdminAuthDaoSQL adminAuthDaoSQL;
    private ManagerAuthDaoSQL managerAuthDaoSQL;
    private UserAuthDaoSQL sellerAuthDaoSQL;

    private UsersServiceMySQLImpl usersServiceMySQL;

    @SuppressWarnings("null")
    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain) throws IOException {
        try {
            System.out.println("filter--------------------------");

            String authorizationHeader = request.getHeader("Authorization");
            String authorizationParam = request.getParameter("auth");
            System.out.println(authorizationHeader + authorizationParam);
            System.out.println("1geuuuuuuuuuuuuuuuuuuuuuu");
            
            if (authorizationHeader == null && authorizationParam == null) {
                filterChain.doFilter(request, response);
                return;
            }

            System.out.println("geuuuuuuuuuuuuuuuuuuuuuu");

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

            System.out.println(userEmail);
            System.out.println(SecurityContextHolder.getContext().getAuthentication());

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                System.out.println("userdetails");
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
                System.out.println(userDetails + "userdetails1");
                System.out.println(82+ "************************************");
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    System.out.println(84);
                    if (!isInDbAndActivated(userDetails, jwt)) {
                        throw new IllegalAccessException("Token invalid");
                    }
                    System.out.println(88);

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
        if (userDetails.getAuthorities().contains(new SimpleGrantedAuthority(ERole.USER.name()))
                && usersServiceMySQL.getByEmail(userDetails.getUsername()).getIsActivated().equals(true)) {
            return sellerAuthDaoSQL.findByAccessToken(jwt) != null; //todo auth dao for everyone
        }
        return false;
    }

}
