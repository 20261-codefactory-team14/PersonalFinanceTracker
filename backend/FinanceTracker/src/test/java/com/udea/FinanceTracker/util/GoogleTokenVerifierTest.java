package com.udea.FinanceTracker.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.security.GeneralSecurityException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para GoogleTokenVerifier.
 * Valida la verificación de tokens de Google OAuth2.
 *
 * Patrón Triple AAA aplicado en cada prueba.
 */
@ExtendWith(MockitoExtension.class)
class GoogleTokenVerifierTest {

    @InjectMocks
    private GoogleTokenVerifier googleTokenVerifier;

    @Mock
    private com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier mockVerifier;

    private static final String GOOGLE_CLIENT_ID = "test-client-id.apps.googleusercontent.com";

    @BeforeEach
    void setUp() {
        // Configurar el clientId mediante reflexión (similar a JwtUtilTest)
        ReflectionTestUtils.setField(googleTokenVerifier, "googleClientId", GOOGLE_CLIENT_ID);
    }

    /**
     * Prueba del camino feliz: Token de Google completo y válido.
     *
     * Tipo de prueba: Funcional positivo
     * Patrón AAA: Arrange, Act, Assert
     *
     * Corresponde al CP-001-A (ya cubierto) - Se incluye por completitud.
     */
    @Test
    void verifyToken_WithValidToken_ReturnsUserInfo() throws Exception {
        // ==================== ARRANGE ====================
        // Configurar datos de prueba: token válido (simulado)
        // Nota: Esta prueba requeriría un token real o un mock más profundo.
        // Para pruebas unitarias puras, se debe mockear el verifier de Google.
        String validIdToken = "eyJhbGciOiJSUzI1NiIsImtpZCI6InRlc3QiLCJ0eXAiOiJKV1QifQ.valid-token";

        // ==================== ACT ====================
        // Ejecutar el método a probar
        // ==================== ASSERT ====================
        // Verificar que se extraen los datos correctamente
        // Implementación pendiente (requiere mock del GoogleIdTokenVerifier)
    }

    /**
     * Prueba de excepción: Token de Google sin campo "email".
     *
     * Tipo de prueba: Validación negativa (Excepción)
     * Patrón AAA: Arrange, Act, Assert
     *
     * Corresponde al CP-001-C: Validación de campos obligatorios desde Google
     */
    @Test
    void verifyToken_WithTokenMissingEmail_ThrowsException() {
        // ==================== ARRANGE ====================
        // Simular un token que no contiene el campo "email"
        // Nota: Esta prueba documenta el comportamiento esperado según HU.
        // Actualmente fallará porque el código no implementa esta validación.

        String tokenWithoutEmail = "eyJhbGciOiJSUzI1NiIsImtpZCI6InRlc3QiLCJ0eXAiOiJKV1QifQ.no-email-token";

        // ==================== ACT & ASSERT ====================
        // Descomentar y completar la aserción
        assertThatThrownBy(() -> googleTokenVerifier.verifyToken(tokenWithoutEmail))
                .isInstanceOf(GeneralSecurityException.class)
                .hasMessageContaining("Failed to extract user info");

        // ==================== NOTA ====================
        // Esta prueba fallará actualmente porque el código no implementa
        // esta validación. Debe ser corregida por el equipo de desarrollo.
        // Se marca como pendiente hasta que se implemente la validación.
    }
}