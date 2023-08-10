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

    // todo key to app.props

    private static final String SECRET_KEY = "404E635268556A586E3272357538782F413F4428472B4B625064536756685970";
    private static final String SECRET_KEY_Manager = "404E635267556A586E3272357538782F413F4428472B4B625064536756685970";
    private static final String SECRET_KEY_Admin = "404E735266557A586E3272357538782F413F4428472B4B625064536756685970";
    private static final String SECRET_KEY_Customer = "404E745266556A586E3272357538782F413F4428472B4B625064536756685970";


    public String extractUsername(String jwt, ERole role) {

        return extractClaim(jwt, Claims::getSubject, role);
    }


    public String extractUsername(String jwt) {
//        for (ERole role : ERole.values()) {
//            System.out.println(role);
        return extractClaim(jwt, Claims::getSubject);
//            System.out.println(username);
//            if (username != null) {
//                return username;
//            }
//        }
//        return null;
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsTFunction, ERole role) {
        Claims claims = extractAllClaims(token, role);
        return claimsTFunction.apply(claims);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsTFunction) {
        Claims claims = Jwts.claims();
        try {
            for (ERole role : ERole.values()) {
                System.out.println(role);
                Claims x = extractAllClaims(token, role);
                if (x !=null) {
                    claims.putAll(x);
                    return claimsTFunction.apply(claims);
                }
            }

        } catch (IllegalArgumentException exception) {}
        catch (SignatureException exception) {}
        catch (JwtException exception) {}
        return claimsTFunction.apply(claims);
    }

    public Claims extractAllClaims(String token, ERole role) throws JwtException {
        Claims claims = null;
//
        try {
            Claims x = Jwts
                    .parserBuilder()
                    .setSigningKey(getSigningKey(role))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            assert claims != null;
            claims = x;
        } catch (SignatureException e) {
            // log the exception
            System.out.println("dont care");
        }
        return claims;
//        throw new JwtException("JWT signature does not match locally computed signature");
    }

//    public Key getSigningKey(String key) {
//        byte[] keyBytes = Decoders.BASE64.decode(key);
//        return Keys.hmacShaKeyFor(keyBytes);
//    }

    public Key getSigningKey(ERole role) {
        String key = null;
        switch (role) {
            case ADMIN:
                key = SECRET_KEY_Admin;
                break;
            case MANAGER:
                key = SECRET_KEY_Manager;
                break;
            case SELLER:
                key = SECRET_KEY;
                break;
            case CUSTOMER:
                key = SECRET_KEY_Customer;
                break;
        }
        System.out.println(key);
        System.out.println("key");
        byte[] keyBytes = Decoders.BASE64.decode(key);
        return Keys.hmacShaKeyFor(keyBytes);
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
//                .signWith(getSigningKey(SECRET_KEY), SignatureAlgorithm.HS256)
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
//                .signWith(getSigningKey(SECRET_KEY), SignatureAlgorithm.HS256)
                .signWith(getSigningKey(ERole.SELLER), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return generateRefreshToken(new HashMap<>(), userDetails);
    }

    public AuthenticationResponse generateTokenPair(
            String key,
            ERole issuer,
            Map<String, String> extraClaims,
            UserDetails userDetails
    ) {
        System.out.println(userDetails);
        System.out.println(userDetails.getUsername());
        System.out.println("details");
        String accessToken = Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setIssuer(issuer.name())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
//                .signWith(getSigningKey(key), SignatureAlgorithm.HS256)
                .signWith(getSigningKey(issuer), SignatureAlgorithm.HS256)
                .compact();

        String refreshToken = Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setIssuer(issuer.name())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
//                .signWith(getSigningKey(key), SignatureAlgorithm.HS256)
                .signWith(getSigningKey(issuer), SignatureAlgorithm.HS256)
                .compact();

        System.out.println(accessToken);
        System.out.println(refreshToken);
        System.out.println("jwt service pair");

        return new AuthenticationResponse(accessToken, refreshToken);
    }

    public AuthenticationResponse generateManagerTokenPair(
            Map<String, String> extraClaims,
            UserDetails userDetails
    ) {
        return generateTokenPair(SECRET_KEY_Manager, ERole.MANAGER, extraClaims, userDetails);
    }

    public AuthenticationResponse generateManagerTokenPair(UserDetails userDetails) {
        return generateManagerTokenPair(new HashMap<>(), userDetails);
    }

    public AuthenticationResponse generateAdminTokenPair(
            Map<String, String> extraClaims,
            UserDetails userDetails
    ) {
        return generateTokenPair(SECRET_KEY_Admin, ERole.ADMIN, extraClaims, userDetails);
    }

    public AuthenticationResponse generateAdminTokenPair(UserDetails userDetails) {
        return generateAdminTokenPair(new HashMap<>(), userDetails);
    }

    public AuthenticationResponse generateCustomerTokenPair(
            Map<String, String> extraClaims,
            UserDetails userDetails
    ) {
        return generateTokenPair(SECRET_KEY_Customer, ERole.CUSTOMER, extraClaims, userDetails);
    }

    public AuthenticationResponse generateCustomerTokenPair(UserDetails userDetails) {
        return generateCustomerTokenPair(new HashMap<>(), userDetails);
    }

    public boolean isTokenValid(String jwt, UserDetails userDetails, ERole role) {
        String username = extractUsername(jwt, role);
        return (username.equals(userDetails.getUsername()) && !isTokenExprired(jwt, role));
    }

    public String extractTokenFromHeader(HttpServletRequest request) {
        String bearerToken = null;

        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            bearerToken = authorizationHeader.substring(7);
        }
        return bearerToken;
    }

    private boolean isTokenExprired(String token, ERole role) {
        return extractExpiration(token, role).before(new Date());
    }

    private Date extractExpiration(String token, ERole role) {
        return extractClaim(token, Claims::getExpiration, role);
    }

}
