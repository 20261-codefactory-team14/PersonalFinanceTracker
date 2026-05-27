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
        objectMapper = new ObjectMapper();
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
}