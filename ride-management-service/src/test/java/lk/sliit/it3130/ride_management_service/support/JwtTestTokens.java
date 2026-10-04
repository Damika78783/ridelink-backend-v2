package lk.sliit.it3130.ride_management_service.support;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/** Builds JWTs signed exactly like Account Service does (subject = email, claim "role"). */
public final class JwtTestTokens {

    // Must equal jwt.secret in src/test/resources/application-test.properties
    private static final String SECRET = "RideLinkTestSecretForAutomatedTestsOnly1234567890";
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    private JwtTestTokens() {
    }

    public static String bearer(String email, String role) {
        return "Bearer " + token(email, role, new Date(System.currentTimeMillis() + 3_600_000));
    }

    public static String expiredBearer(String email, String role) {
        return "Bearer " + token(email, role, new Date(System.currentTimeMillis() - 60_000));
    }

    public static String wrongSignatureBearer(String email, String role) {
        SecretKey other = Keys.hmacShaKeyFor(
                "AnotherCompletelyDifferentSecretKey0987654321".getBytes(StandardCharsets.UTF_8));
        return "Bearer " + Jwts.builder().subject(email).claim("role", role)
                .expiration(new Date(System.currentTimeMillis() + 3_600_000))
                .signWith(other).compact();
    }

    private static String token(String email, String role, Date expiry) {
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(expiry)
                .signWith(KEY)
                .compact();
    }
}
