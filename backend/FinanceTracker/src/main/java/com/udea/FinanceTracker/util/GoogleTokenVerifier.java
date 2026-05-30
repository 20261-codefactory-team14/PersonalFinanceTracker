package com.udea.FinanceTracker.util;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

@Component
public class GoogleTokenVerifier {

    private static final Logger logger = LoggerFactory.getLogger(GoogleTokenVerifier.class);

    @Value("${spring.security.oauth2.client.registration.google.client-id:}")
    private String googleClientId;

    private GoogleIdTokenVerifier verifier;

    /**
     * Verify Google ID Token and extract user information
     */
    public Map<String, String> verifyToken(String idToken) throws GeneralSecurityException, IOException {
        if (googleClientId == null || googleClientId.isEmpty()) {
            logger.error("Google Client ID is not configured");
            throw new RuntimeException("Google Client ID is not configured");
        }

        logger.info("Attempting to verify Google ID token with client ID: {}", googleClientId);

        try {
            // Initialize verifier if not already done
            if (verifier == null) {
                logger.info("client id being used");
                logger.info("Received token");
                logger.info("Token parts");
                logger.info("Initializing GoogleIdTokenVerifier");
                verifier = new GoogleIdTokenVerifier.Builder(
                        new NetHttpTransport(),
                        GsonFactory.getDefaultInstance())
                        .setAudience(Collections.singletonList(googleClientId))
                        .setIssuers(List.of(
                                "https://accounts.google.com",
                                "accounts.google.com"
                        ))
                        .build();
            }

            GoogleIdToken parsedToken = GoogleIdToken.parse(
                    GsonFactory.getDefaultInstance(), idToken
            );

            GoogleIdToken verified = verifier.verify(idToken);
    

            if (verified == null) throw new RuntimeException("Verification returned null");

            // Verify the token
            logger.info("Verifying token...");
            GoogleIdToken idTokenObj = verifier.verify(idToken);
            logger.info("IdTokenOBJ: {}", idTokenObj);

            if (idTokenObj == null) {
                logger.error("Token verification failed - idTokenObj is null. Token may be invalid or audience doesn't match.");
                throw new RuntimeException("Invalid ID token - verification failed");
            }

            logger.info("Token verified successfully");

            // Extract user information from verified token
            GoogleIdToken.Payload payload = idTokenObj.getPayload();

            String googleId = payload.getSubject();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String picture = (String) payload.get("picture");

            logger.info("Successfully extracted user info - Email: {}, GoogleId: {}", email, googleId);

            Map<String, String> userInfo = new HashMap<>();
            userInfo.put("googleId", googleId);
            userInfo.put("email", email);
            userInfo.put("name", name != null ? name : "");
            userInfo.put("picture", picture != null ? picture : "");

            return userInfo;
        } catch (IOException e) {
            logger.error("IOException during token verification: {}", e.getMessage(), e);
            throw new IOException("Failed to verify Google token: " + e.getMessage(), e);
        } catch (GeneralSecurityException e) {
            logger.error("GeneralSecurityException during token verification: {}", e.getMessage(), e);
            throw new GeneralSecurityException("Failed to verify Google token signature: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected exception during token verification: {}", e.getMessage(), e);
            throw new IOException("Failed to verify Google token: " + e.getMessage(), e);
        }
    }
}


