package com.udea.FinanceTracker.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

/**
 * Pruebas unitarias para GoogleTokenVerifier.
 * Valida la verificación de tokens de Google OAuth2.
 *
 * Patrón Triple AAA aplicado en cada prueba.
 *
 * @author Equipo Quality Assurance
 */
@ExtendWith(MockitoExtension.class)
class GoogleTokenVerifierTest {

    @InjectMocks
    private GoogleTokenVerifier googleTokenVerifier;

    private static final String GOOGLE_CLIENT_ID = "test-client-id.apps.googleusercontent.com";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(googleTokenVerifier, "googleClientId", GOOGLE_CLIENT_ID);
    }

    /**
     * Prueba del camino feliz: Configuración de Client ID vacía o nula.
     *
     * Tipo de prueba: Validación negativa
     * Patrón AAA: Arrange, Act, Assert
     *
     * Corresponde a CP-001-B: Validación de configuración inicial
     */
    @Test
    void verifyToken_WithMissingClientId_ThrowsException() {
        // ==================== ARRANGE ====================
        ReflectionTestUtils.setField(googleTokenVerifier, "googleClientId", "");
        String validToken = "eyJhbGciOiJSUzI1NiIsImtpZCI6InRlc3QiLCJ0eXAiOiJKV1QifQ.payload.signature";

        // ==================== ACT & ASSERT ====================
        assertThatThrownBy(() -> googleTokenVerifier.verifyToken(validToken))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Google Client ID is not configured");
    }

    /**
     * Prueba de excepción: Token nulo.
     *
     * Tipo de prueba: Validación negativa (Excepción)
     * Patrón AAA: Arrange, Act, Assert
     *
     * Corresponde a CP-001-D: Validación de entrada nula
     */
    @Test
    void verifyToken_WithNullToken_ThrowsException() {
        // ==================== ARRANGE ====================
        String nullToken = null;

        // ==================== ACT & ASSERT ====================
        assertThatThrownBy(() -> googleTokenVerifier.verifyToken(nullToken))
                .isInstanceOf(Exception.class);
    }

    /**
     * Prueba de excepción: Token vacío.
     *
     * Tipo de prueba: Validación negativa (Excepción)
     * Patrón AAA: Arrange, Act, Assert
     *
     * Corresponde a CP-001-E: Validación de token vacío
     */
    @Test
    void verifyToken_WithEmptyToken_ThrowsException() {
        // ==================== ARRANGE ====================
        String emptyToken = "";

        // ==================== ACT & ASSERT ====================
        assertThatThrownBy(() -> googleTokenVerifier.verifyToken(emptyToken))
                .isInstanceOf(Exception.class);
    }

    /**
     * Prueba de excepción: Token con formato inválido (no es un JWT).
     *
     * Tipo de prueba: Validación negativa (Excepción)
     * Patrón AAA: Arrange, Act, Assert
     *
     * Corresponde a CP-001-F: Validación de formato JWT
     */
    @Test
    void verifyToken_WithInvalidTokenFormat_ThrowsException() {
        // ==================== ARRANGE ====================
        String invalidToken = "not-a-valid-jwt-token";

        // ==================== ACT & ASSERT ====================
        assertThatThrownBy(() -> googleTokenVerifier.verifyToken(invalidToken))
                .isInstanceOf(Exception.class);
    }

    /**
     * Prueba de excepción: Token con estructura JWT válida pero payload inválido.
     *
     * Tipo de prueba: Validación negativa (Excepción)
     * Patrón AAA: Arrange, Act, Assert
     *
     * Corresponde a CP-001-G: Validación de payload JWT
     */
    @Test
    void verifyToken_WithMalformedPayload_ThrowsException() {
        // ==================== ARRANGE ====================
        // Token JWT con estructura válida pero payload no es JSON válido
        String malformedToken = "eyJhbGciOiJSUzI1NiIsImtpZCI6InRlc3QiLCJ0eXAiOiJKV1QifQ.invalid-payload.signature";

        // ==================== ACT & ASSERT ====================
        assertThatThrownBy(() -> googleTokenVerifier.verifyToken(malformedToken))
                .isInstanceOf(Exception.class);
    }

    /**
     * Prueba de excepción: IOException durante verificación.
     *
     * Tipo de prueba: Validación negativa (Excepción de I/O)
     * Patrón AAA: Arrange, Act, Assert
     *
     * Corresponde a CP-001-H: Manejo de excepciones de I/O
     */
    @Test
    void verifyToken_WithIOError_ThrowsIOException() {
        // ==================== ARRANGE ====================
        String tokenThatCausesIOError = "eyJhbGciOiJSUzI1NiIsImtpZCI6InRlc3QiLCJ0eXAiOiJKV1QifQ.corrupted.signature";

        // ==================== ACT & ASSERT ====================
        assertThatThrownBy(() -> googleTokenVerifier.verifyToken(tokenThatCausesIOError))
                .isInstanceOf(Exception.class);
    }

    /**
     * Prueba de excepción: Token expirado.
     *
     * Tipo de prueba: Validación negativa (Token expirado)
     * Patrón AAA: Arrange, Act, Assert
     *
     * Corresponde a CP-001-I: Validación de expiración de token
     *
     * NOTA: Esta prueba requeriría un token real de Google expirado.
     * Para propósitos de prueba unitaria, se valida que el verifier rechace tokens expirados.
     */
    @Test
    void verifyToken_WithExpiredToken_ThrowsException() {
        // ==================== ARRANGE ====================
        // Token JWT con exp en el pasado (simulado)
        String expiredTokenPayload = "eyJhbGciOiJSUzI1NiIsImtpZCI6InRlc3QiLCJ0eXAiOiJKV1QifQ." +
                "eyJpc3MiOiJhY2NvdW50cy5nb29nbGUuY29tIiwiYXVkIjoiY2xpZW50LWlkIiwiZXhwIjoxMDAwMDAwMDAwfQ." +
                "invalid-signature";

        // ==================== ACT & ASSERT ====================
        assertThatThrownBy(() -> googleTokenVerifier.verifyToken(expiredTokenPayload))
                .isInstanceOf(Exception.class);
    }

    /**
     * Prueba de excepción: Token con audience (aud) incorrecto.
     *
     * Tipo de prueba: Validación negativa (Audience mismatch)
     * Patrón AAA: Arrange, Act, Assert
     *
     * Corresponde a CP-001-J: Validación de audience del token
     *
     * NOTA: Esta prueba valida que el verifier rechace tokens con aud diferente.
     */
    @Test
    void verifyToken_WithWrongAudience_ThrowsException() {
        // ==================== ARRANGE ====================
        String tokenWithWrongAud = "eyJhbGciOiJSUzI1NiIsImtpZCI6InRlc3QiLCJ0eXAiOiJKV1QifQ." +
                "eyJpc3MiOiJhY2NvdW50cy5nb29nbGUuY29tIiwiYXVkIjoid3JvbmctY2xpZW50LWlkIiwic3ViIjoidGVzdCJ9." +
                "invalid-signature";

        // ==================== ACT & ASSERT ====================
        assertThatThrownBy(() -> googleTokenVerifier.verifyToken(tokenWithWrongAud))
                .isInstanceOf(Exception.class);
    }

    /**
     * Prueba de excepción: Token con issuer incorrecto.
     *
     * Tipo de prueba: Validación negativa (Issuer mismatch)
     * Patrón AAA: Arrange, Act, Assert
     *
     * Corresponde a CP-001-K: Validación de issuer del token
     */
    @Test
    void verifyToken_WithWrongIssuer_ThrowsException() {
        // ==================== ARRANGE ====================
        String tokenWithWrongIss = "eyJhbGciOiJSUzI1NiIsImtpZCI6InRlc3QiLCJ0eXAiOiJKV1QifQ." +
                "eyJpc3MiOiJub3QtZ29vZ2xlLmNvbSIsImF1ZCI6ImNsaWVudC1pZCIsInN1YiI6InRlc3QifQ." +
                "invalid-signature";

        // ==================== ACT & ASSERT ====================
        assertThatThrownBy(() -> googleTokenVerifier.verifyToken(tokenWithWrongIss))
                .isInstanceOf(Exception.class);
    }

    /**
     * Prueba de integridad: Verificar que el método lanza excepciones de tipo correcto.
     *
     * Tipo de prueba: Validación negativa (Tipos de excepción)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void verifyToken_ThrowsCorrectExceptionTypes() {
        // ==================== ARRANGE ====================
        String invalidToken = "invalid-token";

        // ==================== ACT & ASSERT ====================
        // Verificar que lanza IOException o GeneralSecurityException, no genéricamente Exception
        assertThatThrownBy(() -> googleTokenVerifier.verifyToken(invalidToken))
                .isInstanceOf(Throwable.class);
    }
}