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

/**
 * Componente para verificar tokens de Google ID y extraer información del usuario.
 *
 * @author Equipo Quality Assurance
 * @version 1.1
 */
@Component
public class GoogleTokenVerifier {

    private static final Logger logger = LoggerFactory.getLogger(GoogleTokenVerifier.class);

    @Value("${spring.security.oauth2.client.registration.google.client-id:}")
    private String googleClientId;

    private GoogleIdTokenVerifier verifier;

    /**
     * Verifica un token de Google ID y extrae la información del usuario.
     *
     * ==================== CORRECCIÓN DE BUG-01 Google token sin email no validado  ====================
     * PROBLEMA: El método lanzaba RuntimeException cuando fallaba la verificación
     * del token o cuando faltaban campos obligatorios. Esto causaba que el test
     * verifyToken_WithTokenMissingEmail_ThrowsException fallara porque esperaba
     * una excepción de tipo GeneralSecurityException.
     *
     * SOLUCIÓN:
     * 1. Se reemplazaron todos los throw new RuntimeException() por
     *    throw new GeneralSecurityException() para cumplir con la firma del método
     *    y las expectativas del test.
     * 2. Se agregó un bloque catch (Exception e) al final para capturar cualquier
     *    excepción inesperada y convertirla a GeneralSecurityException.
     * 3. Se agregó validación de campos obligatorios (email y name) que también
     *    lanza GeneralSecurityException.
     *
     * RESULTADO: El test ahora recibe el tipo de excepción esperado y pasa correctamente.
     * ==================== FIN CORRECCIÓN ====================
     *
     * @param idToken Token de Google ID a verificar
     * @return Mapa con la información del usuario (googleId, email, name, picture)
     * @throws GeneralSecurityException Si ocurre un error de seguridad durante la verificación
     * @throws IOException Si ocurre un error de entrada/salida durante la verificación
     */
    public Map<String, String> verifyToken(String idToken) throws GeneralSecurityException, IOException {
        // Validar configuración del Client ID
        if (googleClientId == null || googleClientId.isEmpty()) {
            logger.error("Google Client ID is not configured");
            throw new GeneralSecurityException("Google Client ID is not configured");
        }

        logger.info("Attempting to verify Google ID token with client ID: {}", googleClientId);

        try {
            // Inicializar el verificador si no existe
            if (verifier == null) {
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

            // Verificar el token
            logger.info("Verifying token...");
            GoogleIdToken idTokenObj = verifier.verify(idToken);

            if (idTokenObj == null) {
                logger.error("Token verification failed - Token may be invalid or audience doesn't match.");
                throw new GeneralSecurityException("Failed to extract user info from Google token");
            }

            logger.info("Token verified successfully");

            // Extraer información del usuario del token verificado
            GoogleIdToken.Payload payload = idTokenObj.getPayload();

            String googleId = payload.getSubject();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String picture = (String) payload.get("picture");

            // Validación de campos obligatorios según HU 1.1
            // El token debe contener email y name para ser válido
            if (email == null || email.trim().isEmpty()) {
                logger.error("Google token missing required field: email");
                throw new GeneralSecurityException("Email es obligatorio en el token de Google");
            }
            if (name == null || name.trim().isEmpty()) {
                logger.error("Google token missing required field: name");
                throw new GeneralSecurityException("Name es obligatorio en el token de Google");
            }

            logger.info("Successfully extracted user info - Email: {}, GoogleId: {}", email, googleId);

            // Construir mapa con la información del usuario
            Map<String, String> userInfo = new HashMap<>();
            userInfo.put("googleId", googleId);
            userInfo.put("email", email);
            userInfo.put("name", name);
            userInfo.put("picture", picture != null ? picture : "");

            return userInfo;

        } catch (GeneralSecurityException e) {
            // Relanzar excepciones de seguridad
            logger.error("Security exception during token verification: {}", e.getMessage());
            throw e;
        } catch (IOException e) {
            // Convertir IOException a GeneralSecurityException
            logger.error("IO exception during token verification: {}", e.getMessage());
            throw new GeneralSecurityException("Failed to verify Google token: " + e.getMessage(), e);
        } catch (Exception e) {
            // Capturar cualquier otra excepción y convertirla a GeneralSecurityException
            logger.error("Unexpected exception during token verification: {}", e.getMessage(), e);
            throw new GeneralSecurityException("Unexpected error during token verification: " + e.getMessage(), e);
        }
    }
}