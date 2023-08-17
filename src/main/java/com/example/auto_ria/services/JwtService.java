package com.example.auto_ria.services;

import com.example.auto_ria.enums.ERole;
import com.example.auto_ria.models.responses.AuthenticationResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@AllArgsConstructor
public class JwtService {

    @Value("${token.generation.key.seller}")
    private static final String SECRET_KEY = "404E635268556A586E3272357538782F413F4428472B4B625064536756685970";

    @Value("${token.generation.key.manager}")
    private static final String SECRET_KEY_Manager = "404E635267556A586E3272357538782F413F4428472B4B625064536756685970";

    @Value("${token.generation.key.admin}")
    private static final String SECRET_KEY_Admin = "404E735266557A586E3272357538782F413F4428472B4B625064536756685970";

    @Value("${token.generation.key.customer}")
    private static final String SECRET_KEY_Customer = "404E745266556A586E3272357538782F413F4428472B4B625064536756685970";

    public String extractUsername(String jwt, ERole role) {
        return extractClaim(jwt, Claims::getSubject, role);
    }


    public String extractUsername(String jwt) {
        return extractClaim(jwt, Claims::getSubject);
    }

    public String extractAudience(String jwt) {
        return extractClaim(jwt, Claims::getAudience);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsTFunction, ERole role) {
        Claims claims = extractAllClaims(token, role);
        return claimsTFunction.apply(claims);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsTFunction) {
        Claims claims = Jwts.claims();
        try {
            for (ERole role : ERole.values()) {
                Claims result = extractAllClaims(token, role);
                if (result != null) {
                    claims.putAll(result);
                    return claimsTFunction.apply(claims);
                }
            }

        } catch (IllegalArgumentException | SignatureException ignored) {
        }
        return claimsTFunction.apply(claims);
    }

    public Claims extractAllClaims(String token, ERole role) throws JwtException {
        Claims claims = null;
        try {
            claims = Jwts
                    .parserBuilder()
                    .setSigningKey(getSigningKey(role))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

        } catch (SignatureException ignored) {
        }
        return claims;
    }

    public Key getSigningKey(ERole role) {
        String key = switch (role) {
            case ADMIN -> SECRET_KEY_Admin;
            case MANAGER -> SECRET_KEY_Manager;
            case SELLER -> SECRET_KEY;
            case CUSTOMER -> SECRET_KEY_Customer;
            default -> null;
        };

        byte[] keyBytes = Decoders.BASE64.decode(key);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Key getRegisterKey(ERole role) {
        String key = switch (role) {
//            case ADMIN -> REGISTER_KEY_Admin;
//            case MANAGER -> REGISTER_KEY_Manager;
            case ADMIN -> SECRET_KEY_Admin;
            case MANAGER -> SECRET_KEY_Manager;
            default -> null;
        };

        byte[] keyBytes = Decoders.BASE64.decode(key);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateRegisterKey(
            Map<String, Object> extraClaims,
            String email,
            ERole role
    ) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(email)
                .setAudience(role.name())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(getRegisterKey(role), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRegisterKey(String email, ERole role) {
        return generateRegisterKey(new HashMap<>(), email, role);
    }

    public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    ) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setIssuer(ERole.SELLER.name())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(getSigningKey(ERole.SELLER), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateRefreshToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    ) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setIssuer(ERole.SELLER.name())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                .signWith(getSigningKey(ERole.SELLER), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return generateRefreshToken(new HashMap<>(), userDetails);
    }

    public AuthenticationResponse generateTokenPair(
            ERole issuer,
            Map<String, String> extraClaims,
            UserDetails userDetails
    ) {
        String accessToken = Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setIssuer(issuer.name())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(getSigningKey(issuer), SignatureAlgorithm.HS256)
                .compact();

        String refreshToken = Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setIssuer(issuer.name())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                .signWith(getSigningKey(issuer), SignatureAlgorithm.HS256)
                .compact();

        return new AuthenticationResponse(accessToken, refreshToken);
    }

    public AuthenticationResponse generateManagerTokenPair(
            Map<String, String> extraClaims,
            UserDetails userDetails
    ) {
        return generateTokenPair(ERole.MANAGER, extraClaims, userDetails);
    }

    public AuthenticationResponse generateManagerTokenPair(UserDetails userDetails) {
        return generateManagerTokenPair(new HashMap<>(), userDetails);
    }

    public AuthenticationResponse generateAdminTokenPair(
            Map<String, String> extraClaims,
            UserDetails userDetails
    ) {
        return generateTokenPair(ERole.ADMIN, extraClaims, userDetails);
    }

    public AuthenticationResponse generateAdminTokenPair(UserDetails userDetails) {
        return generateAdminTokenPair(new HashMap<>(), userDetails);
    }

    public AuthenticationResponse generateCustomerTokenPair(
            Map<String, String> extraClaims,
            UserDetails userDetails
    ) {
        return generateTokenPair(ERole.CUSTOMER, extraClaims, userDetails);
    }

    public AuthenticationResponse generateCustomerTokenPair(UserDetails userDetails) {
        return generateCustomerTokenPair(new HashMap<>(), userDetails);
    }

    public boolean isTokenValid(String jwt, UserDetails userDetails) {
        String username = extractUsername(jwt);
        return (username.equals(userDetails.getUsername()) && !isTokenExprired(jwt));
    }

    public boolean isKeyValid(String jwt, String email, ERole role) {
        String username = extractUsername(jwt, role);
        return (username.equals(email) && !isTokenExprired(jwt));
    }

    public String extractTokenFromHeader(HttpServletRequest request) {
        String bearerToken = null;

        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            bearerToken = authorizationHeader.substring(7);
        }
        return bearerToken;
    }

    private boolean isTokenExprired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

}
