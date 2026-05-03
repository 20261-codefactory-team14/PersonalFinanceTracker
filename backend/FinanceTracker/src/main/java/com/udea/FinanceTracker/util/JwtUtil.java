package com.udea.FinanceTracker.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret:mySecretKeyForFinanceTrackerApplicationThatIsAtLeast32CharactersLong}")
    private String secretKey;

    @Value("${jwt.expiration:86400000}")
    private Long jwtExpiration; // 24 hours in milliseconds

    @Value("${jwt.refresh-expiration:604800000}")
    private Long refreshTokenExpiration; // 7 days in milliseconds

    /**
     * Generate JWT token
     */
    public String generateToken(String email) {
        return createToken(email, null, jwtExpiration);
    }

    /**
     * Generate JWT token with userId claim
     */
    public String generateToken(String email, Long userId) {
        return createToken(email, userId, jwtExpiration);
    }

    /**
     * Generate refresh token
     */
    public String generateRefreshToken(String email) {
        return createToken(email, null, refreshTokenExpiration);
    }

    /**
     * Generate refresh token with userId claim
     */
    public String generateRefreshToken(String email, Long userId) {
        return createToken(email, userId, refreshTokenExpiration);
    }

    /**
     * Create JWT token with custom expiration
     */
    private String createToken(String email, Long userId, Long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        var builder = Jwts.builder()
                .subject(email)
                .issuedAt(now)
                .expiration(expiryDate);

        // Add userId claim if provided
        if (userId != null) {
            builder.claim("userId", userId);
        }

        return builder
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .compact();
    }

    /**
     * Extract email from token
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract userId from token
     */
    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    /**
     * Extract specific claim from token
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract all claims from token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Check if token is expired
     */
    private Boolean isTokenExpired(String token) {
        try {
            Date expiration = extractClaim(token, Claims::getExpiration);
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Validate token
     */
    public Boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                    .build()
                    .parseSignedClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validate token with email
     */
    public Boolean validateToken(String token, String email) {
        final String tokenEmail = extractEmail(token);
        return (tokenEmail.equals(email) && !isTokenExpired(token));
    }
}

