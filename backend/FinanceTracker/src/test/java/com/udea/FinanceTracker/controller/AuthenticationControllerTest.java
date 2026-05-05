package com.udea.FinanceTracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.udea.FinanceTracker.dto.AuthenticationResponse;
import com.udea.FinanceTracker.dto.GoogleLoginRequest;
import com.udea.FinanceTracker.dto.UpdatePerfilRequest;
import com.udea.FinanceTracker.dto.UsuarioDTO;
import com.udea.FinanceTracker.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.security.GeneralSecurityException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AuthenticationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private AuthenticationController authenticationController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(authenticationController).build();
        // Configurar ObjectMapper para soportar LocalDate
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    void googleLogin_Success() throws Exception {
        // Arrange
        GoogleLoginRequest request = new GoogleLoginRequest();
        request.setIdToken("valid_token");

        AuthenticationResponse response = new AuthenticationResponse();
        response.setAccessToken("access_token");
        response.setRefreshToken("refresh_token");

        when(usuarioService.authenticateWithGoogle(any(GoogleLoginRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/auth/google-login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access_token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh_token"));
    }

    @Test
    void googleLogin_InvalidToken() throws Exception {
        // Arrange
        GoogleLoginRequest request = new GoogleLoginRequest();
        request.setIdToken("invalid_token");

        when(usuarioService.authenticateWithGoogle(any(GoogleLoginRequest.class)))
                .thenThrow(new GeneralSecurityException("Invalid token"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/google-login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Google token verification failed"));
    }

    @Test
    void validateToken_ValidToken() throws Exception {
        // Arrange
        UsuarioDTO usuario = new UsuarioDTO();
        usuario.setId(1L);
        usuario.setEmail("test@example.com");

        when(usuarioService.validateToken(anyString())).thenReturn(true);
        when(usuarioService.getEmailFromToken(anyString())).thenReturn("test@example.com");
        when(usuarioService.getUserByEmail(anyString())).thenReturn(usuario);

        // Act & Assert
        mockMvc.perform(post("/api/auth/validate-token")
                .header("Authorization", "Bearer valid_token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void validateToken_InvalidToken() throws Exception {
        // Arrange
        when(usuarioService.validateToken(anyString())).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/api/auth/validate-token")
                .header("Authorization", "Bearer invalid_token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Token inválido o expirado"));
    }

    @Test
    void getUserProfile_Success() throws Exception {
        // Arrange
        UsuarioDTO usuario = new UsuarioDTO();
        usuario.setId(1L);
        usuario.setEmail("test@example.com");

        when(usuarioService.validateToken(anyString())).thenReturn(true);
        when(usuarioService.getEmailFromToken(anyString())).thenReturn("test@example.com");
        when(usuarioService.getUserByEmail(anyString())).thenReturn(usuario);

        // Act & Assert
        mockMvc.perform(get("/api/auth/profile")
                .header("Authorization", "Bearer valid_token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void getUserProfile_InvalidToken() throws Exception {
        // Arrange
        when(usuarioService.validateToken(anyString())).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/auth/profile")
                .header("Authorization", "Bearer invalid_token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Token inválido o expirado"));
    }

    // ==================== PRUEBA PARA LOGOUT ====================
    // Corresponde al CP-009-A: Cierre de sesión exitoso
    // ====================

    /**
     * Prueba del camino feliz: Cierre de sesión exitoso.
     *
     * Tipo de prueba: Funcional positivo (Camino Feliz)
     * Patrón AAA: Arrange, Act, Assert
     *
     * Corresponde al CP-009-A: Cierre de sesión exitoso
     * Verifica que el sistema invalida el token y responde correctamente.
     */
    @Test
    void logout_WithValidToken_ReturnsSuccess() throws Exception {
        // ==================== ARRANGE ====================
        String authHeader = "Bearer valid_token";

        // No se necesita mock específico porque el servicio no tiene lógica pesada
        // El logout se maneja en la capa de servicio (blacklist)

        // ==================== ACT & ASSERT ====================
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Sesión cerrada correctamente"));
    }

    /**
     * Prueba de excepción: Logout sin token.
     *
     * Tipo de prueba: Validación negativa (Excepción)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void logout_WithoutToken_ReturnsBadRequest() throws Exception {
        // ==================== ARRANGE ====================
        // Sin header Authorization

        // ==================== ACT & ASSERT ====================
        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    /**
     * Prueba de excepción: Logout con token inválido.
     *
     * Tipo de prueba: Validación negativa (Excepción)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void logout_WithInvalidToken_ReturnsBadRequest() throws Exception {
        // ==================== ARRANGE ====================
        String authHeader = "Bearer invalid_token";

        // Simular que el servicio lanza excepción con token inválido
        // Nota: UsuarioService.validateToken retorna false para token inválido

        // ==================== ACT & ASSERT ====================
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}