package com.example.auto_ria.services.auth;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.core.env.Environment;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.example.auto_ria.enums.ETokenRole;
import com.example.auto_ria.models.responses.auth.AuthenticationResponse;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class JwtService {

    private Environment environment;


    public String extractUsername(String jwt, ETokenRole role) {
        String claim;
        try {
            claim = extractClaim(jwt, Claims::getSubject, role);
        } catch (NullPointerException e) {
            return null;
        }
        return claim;
    }

    public String extractUsername(String jwt) {
        return extractClaim(jwt, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsTFunction, ETokenRole role) {
        Claims claims = extractAllClaims(token, role);
        return claimsTFunction.apply(claims);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsTFunction) {
        Claims claims = Jwts.claims();
        try {
            for (ETokenRole role : ETokenRole.values()) {
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

    public Claims extractClaimsCycle(String token) {
        Claims claims = Jwts.claims();
        try {
            for (ETokenRole role : ETokenRole.values()) {
                Claims result = extractAllClaims(token, role);
                if (result != null) {
                    claims.putAll(result);
                    return claims;
                }
            }

        } catch (IllegalArgumentException | SignatureException ignored) {
        }
        return claims;
    }

    public Claims extractAllClaims(String token, ETokenRole role) throws JwtException {
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

    public Key getSigningKey(ETokenRole role) {
        String key = switch (role) {
            case USER -> environment.getProperty("token.generation.key.user");
            case ADMIN -> environment.getProperty("token.generation.key.admin");
            case MANAGER -> environment.getProperty("token.generation.key.manager");
            case ADMIN_REGISTER -> environment.getProperty("token.register.key.admin");
            case MANAGER_REGISTER -> environment.getProperty("token.register.key.manager");
            case USER_ACTIVATE -> environment.getProperty("token.activate.key.user");
            case FORGOT_PASSWORD -> environment.getProperty("token.forgot.pass.key");
        };

        byte[] keyBytes = Decoders.BASE64.decode(key);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateRegisterKey(
            Map<String, Object> extraClaims,
            String email,
            ETokenRole role) {

        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(email)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(getSigningKey(role), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRegisterKey(String email, ETokenRole role) {
        Map<String, Object> args = new HashMap<>();
        args.put("email", email);
        args.put("recognition", role);
        return generateRegisterKey(args, email, role);
    }

    public String generateCode(
            ETokenRole issuer,
            Map<String, String> extraClaims,
            String userDetails) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setIssuer(issuer.name())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(getSigningKey(issuer), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRegistrationCode(
            Map<String, String> extraClaims,
            String userDetails,
            ETokenRole role) {
        return generateCode(role, extraClaims, userDetails);
    }

    public AuthenticationResponse generateTokenPair(
            ETokenRole issuer,
            Map<String, String> extraClaims,
            UserDetails userDetails) {
        String accessToken = Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setIssuer(issuer.name())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                // .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60))
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
            UserDetails userDetails) {
        return generateTokenPair(ETokenRole.MANAGER, extraClaims, userDetails);
    }

    public AuthenticationResponse generateManagerTokenPair(UserDetails userDetails) {
        return generateManagerTokenPair(new HashMap<>(), userDetails);
    }

    public AuthenticationResponse generateAdminTokenPair(
            Map<String, String> extraClaims,
            UserDetails userDetails) {
        return generateTokenPair(ETokenRole.ADMIN, extraClaims, userDetails);
    }

    public AuthenticationResponse generateAdminTokenPair(UserDetails userDetails) {
        return generateAdminTokenPair(new HashMap<>(), userDetails);
    }

    public AuthenticationResponse generateUserTokenPair(
            Map<String, String> extraClaims,
            UserDetails userDetails) {
        return generateTokenPair(ETokenRole.USER, extraClaims, userDetails);
    }

    public AuthenticationResponse generateUserTokenPair(UserDetails userDetails) {
        return generateUserTokenPair(new HashMap<>(), userDetails);
    }

    public boolean isTokenValid(String jwt, UserDetails userDetails) {
        String username = extractUsername(jwt);
        return (username.equals(userDetails.getUsername()) && !isTokenExprired(jwt));
    }

    public boolean isKeyValid(String jwt, String email, ETokenRole role) {
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

    public boolean isTokenExprired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

}
