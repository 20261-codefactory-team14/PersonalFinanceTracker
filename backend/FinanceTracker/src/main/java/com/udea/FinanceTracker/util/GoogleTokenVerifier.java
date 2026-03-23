package com.udea.FinanceTracker.util;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class GoogleTokenVerifier {

    @Value("${google.client-id:}")
    private String googleClientId;

    private GoogleIdTokenVerifier verifier;

    /**
     * Verify Google ID Token and extract user information
     */
    public Map<String, String> verifyToken(String idToken) throws GeneralSecurityException, IOException {
        if (googleClientId == null || googleClientId.isEmpty()) {
            throw new RuntimeException("Google Client ID is not configured");
        }

        if (verifier == null) {
            verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();
        }

        GoogleIdToken token = verifier.verify(idToken);
        if (token == null) {
            throw new RuntimeException("Invalid ID token.");
        }

        GoogleIdToken.Payload payload = token.getPayload();

        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("googleId", payload.getSubject());
        userInfo.put("email", payload.getEmail());
        userInfo.put("name", (String) payload.get("name"));
        userInfo.put("picture", (String) payload.get("picture"));

        return userInfo;
    }
}

