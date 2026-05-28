package com.udea.FinanceTracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.udea.FinanceTracker.dto.DeleteAccountRequest;
import com.udea.FinanceTracker.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas unitarias para UsuarioController.
 * Valida los endpoints de gestión de usuarios.
 *
 * Patrón Triple AAA aplicado en cada prueba.
 *
 * @author Equipo Quality Assurance
 */
class UsuarioControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private UsuarioController usuarioController;

    private ObjectMapper objectMapper;

    private static final String VALID_JWT_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwiZW1haWwiOiJ0ZXN0QGV4YW1wbGUuY29tIn0.valid";
    private static final String INVALID_JWT_TOKEN = "invalid-token";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(usuarioController)
                .setControllerAdvice()
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Prueba del camino feliz: Eliminación exitosa de cuenta de usuario.
     *
     * Tipo de prueba: Funcional positivo (Camino feliz)
     * Patrón AAA: Arrange, Act, Assert
     *
     * Corresponde a: Caso de uso de eliminación de cuenta exitosa
     *
     * Escenario: Usuario autenticado, token válido, confirmación = true
     * Resultado esperado: Código 200 OK, cuenta eliminada
     */
    @Test
    void deleteAccount_WithValidTokenAndConfirmation_ReturnsSuccess() throws Exception {
        // ==================== ARRANGE ====================
        DeleteAccountRequest request = new DeleteAccountRequest();
        request.setConfirm(true);

        given(usuarioService.deleteUser(VALID_JWT_TOKEN)).willReturn(true);

        // ==================== ACT & ASSERT ====================
        mockMvc.perform(delete("/api/usuario/delete")
                        .header("Authorization", "Bearer " + VALID_JWT_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Sesión finalizada"));
    }

    /**
     * Prueba de excepción: Intento de eliminación sin token de autenticación.
     *
     * Tipo de prueba: Validación negativa (Autenticación faltante)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void deleteAccount_WithoutAuthorizationHeader_ReturnsBadRequest() throws Exception {
        // ==================== ARRANGE ====================
        DeleteAccountRequest request = new DeleteAccountRequest();
        request.setConfirm(true);

        // ==================== ACT & ASSERT ====================
        mockMvc.perform(delete("/api/usuario/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());
    }

    /**
     * Prueba de excepción: Token con formato inválido (sin "Bearer").
     *
     * Tipo de prueba: Validación negativa (Formato de token inválido)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void deleteAccount_WithInvalidTokenFormat_ReturnsBadRequest() throws Exception {
        // ==================== ARRANGE ====================
        DeleteAccountRequest request = new DeleteAccountRequest();
        request.setConfirm(true);

        // ==================== ACT & ASSERT ====================
        mockMvc.perform(delete("/api/usuario/delete")
                        .header("Authorization", INVALID_JWT_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Formato de token inválido"));
    }

    /**
     * Prueba de excepción: Token vacío en Authorization header.
     *
     * Tipo de prueba: Validación negativa (Token vacío)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void deleteAccount_WithEmptyToken_ReturnsBadRequest() throws Exception {
        // ==================== ARRANGE ====================
        DeleteAccountRequest request = new DeleteAccountRequest();
        request.setConfirm(true);

        // ==================== ACT & ASSERT ====================
        mockMvc.perform(delete("/api/usuario/delete")
                        .header("Authorization", "Bearer ")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Token vacío"));
    }

    /**
     * Prueba de excepción: Confirmación = false.
     *
     * Tipo de prueba: Validación negativa (Falta de confirmación)
     * Patrón AAA: Arrange, Act, Assert
     *
     * Corresponde a: Usuario no confirma la eliminación de cuenta
     */
    @Test
    void deleteAccount_WithoutConfirmation_ReturnsBadRequest() throws Exception {
        // ==================== ARRANGE ====================
        DeleteAccountRequest request = new DeleteAccountRequest();
        request.setConfirm(false);

        // ==================== ACT & ASSERT ====================
        mockMvc.perform(delete("/api/usuario/delete")
                        .header("Authorization", "Bearer " + VALID_JWT_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Confirmación requerida"));
    }

    /**
     * Prueba de excepción: Confirmación = null.
     *
     * Tipo de prueba: Validación negativa (Confirmación nula)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void deleteAccount_WithNullConfirmation_ReturnsBadRequest() throws Exception {
        // ==================== ARRANGE ====================
        DeleteAccountRequest request = new DeleteAccountRequest();
        request.setConfirm(null);

        // ==================== ACT & ASSERT ====================
        mockMvc.perform(delete("/api/usuario/delete")
                        .header("Authorization", "Bearer " + VALID_JWT_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Prueba de excepción: Usuario intenta eliminar cuenta de otro usuario (error de acceso).
     *
     * Tipo de prueba: Validación negativa (Acceso denegado)
     * Patrón AAA: Arrange, Act, Assert
     *
     * Corresponde a: Validación de seguridad - solo el propietario puede eliminar
     */
    @Test
    void deleteAccount_WithUnauthorizedAccess_ReturnsForbidden() throws Exception {
        // ==================== ARRANGE ====================
        DeleteAccountRequest request = new DeleteAccountRequest();
        request.setConfirm(true);

        given(usuarioService.deleteUser(VALID_JWT_TOKEN))
                .willThrow(new IllegalAccessException("No tienes permiso para eliminar esta cuenta"));

        // ==================== ACT & ASSERT ====================
        mockMvc.perform(delete("/api/usuario/delete")
                        .header("Authorization", "Bearer " + VALID_JWT_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Acceso denegado"));
    }

    /**
     * Prueba de excepción: Token expirado o inválido.
     *
     * Tipo de prueba: Validación negativa (Token inválido)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void deleteAccount_WithExpiredToken_ReturnsServerError() throws Exception {
        // ==================== ARRANGE ====================
        DeleteAccountRequest request = new DeleteAccountRequest();
        request.setConfirm(true);

        given(usuarioService.deleteUser(VALID_JWT_TOKEN))
                .willThrow(new RuntimeException("Token expirado o inválido"));

        // ==================== ACT & ASSERT ====================
        mockMvc.perform(delete("/api/usuario/delete")
                        .header("Authorization", "Bearer " + VALID_JWT_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    /**
     * Prueba de excepción: Usuario no encontrado.
     *
     * Tipo de prueba: Validación negativa (Usuario inexistente)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void deleteAccount_WithNonexistentUser_ReturnsServerError() throws Exception {
        // ==================== ARRANGE ====================
        DeleteAccountRequest request = new DeleteAccountRequest();
        request.setConfirm(true);

        given(usuarioService.deleteUser(VALID_JWT_TOKEN))
                .willThrow(new RuntimeException("Usuario no encontrado"));

        // ==================== ACT & ASSERT ====================
        mockMvc.perform(delete("/api/usuario/delete")
                        .header("Authorization", "Bearer " + VALID_JWT_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    /**
     * Prueba de excepción: Error del servicio durante eliminación.
     *
     * Tipo de prueba: Validación negativa (Error del servidor)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void deleteAccount_WithServiceError_ReturnsServerError() throws Exception {
        // ==================== ARRANGE ====================
        DeleteAccountRequest request = new DeleteAccountRequest();
        request.setConfirm(true);

        given(usuarioService.deleteUser(VALID_JWT_TOKEN)).willReturn(false);

        // ==================== ACT & ASSERT ====================
        mockMvc.perform(delete("/api/usuario/delete")
                        .header("Authorization", "Bearer " + VALID_JWT_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Error inesperado"));
    }

    /**
     * Prueba de validación: Header Authorization con espacios en blanco solo.
     *
     * Tipo de prueba: Validación negativa (Header vacío)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void deleteAccount_WithOnlyWhitespaceInHeader_ReturnsUnauthorized() throws Exception {
        // ==================== ARRANGE ====================
        DeleteAccountRequest request = new DeleteAccountRequest();
        request.setConfirm(true);

        // ==================== ACT & ASSERT ====================
        mockMvc.perform(delete("/api/usuario/delete")
                        .header("Authorization", "   ")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}

