package com.udea.FinanceTracker.util;

import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

@Component
public class GoogleTokenVerifier {

    @Value("${google.client-id:}")
    private String googleClientId;

    /**
     * Verify Google ID Token and extract user information
     */
    public Map<String, String> verifyToken(String idToken) throws GeneralSecurityException, IOException {
        if (googleClientId == null || googleClientId.isEmpty()) {
            throw new RuntimeException("Google Client ID is not configured");
        }

        try {
            // Parse the JWT token
            SignedJWT signedJWT = (SignedJWT) JWTParser.parse(idToken);

            // Extract claims
            Map<String, Object> claims = signedJWT.getJWTClaimsSet().getClaims();

            // Verify that the token is for our application
            String tokenClientId = (String) claims.get("aud");
            if (!googleClientId.equals(tokenClientId)) {
                throw new RuntimeException("Token audience does not match application client ID");
            }

            Map<String, String> userInfo = new HashMap<>();
            userInfo.put("googleId", (String) claims.get("sub"));
            userInfo.put("email", (String) claims.get("email"));
            userInfo.put("name", (String) claims.get("name"));
            userInfo.put("picture", (String) claims.get("picture"));

            return userInfo;
        } catch (Exception e) {
            throw new IOException("Failed to verify Google token: " + e.getMessage(), e);
        }
    }
}

