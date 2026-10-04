package com.ridelink.drivervehicle.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class JwtUtil {

    private final SecretKey secretKey;

    public JwtUtil(@Value("${ridelink.security.jwt.secret}") String secret) {
        // Ensure secret has sufficient length for HS256 (32 bytes)
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, Math.min(keyBytes.length, 32));
            this.secretKey = Keys.hmacShaKeyFor(padded);
        } else {
            this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(this.secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public String extractSubject(String token) {
        return getClaims(token).getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Claims claims = getClaims(token);
        Object rolesObj = claims.get("roles");
        if (rolesObj instanceof List<?>) {
            return (List<String>) rolesObj;
        }
        Object roleObj = claims.get("role");
        if (roleObj instanceof String) {
            return List.of((String) roleObj);
        }
        return List.of("ROLE_USER");
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(this.secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Utility method to generate demonstration/test tokens.
     */
    public String generateToken(String subject, List<String> roles, long expirationMillis) {
        return Jwts.builder()
                .subject(subject)
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(this.secretKey)
                .compact();
    }
}
