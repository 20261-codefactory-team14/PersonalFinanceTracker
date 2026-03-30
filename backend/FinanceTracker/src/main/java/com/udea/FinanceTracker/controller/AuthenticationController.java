package com.udea.FinanceTracker.controller;

import com.udea.FinanceTracker.dto.AuthenticationResponse;
import com.udea.FinanceTracker.dto.GoogleLoginRequest;
import com.udea.FinanceTracker.dto.UpdatePerfilRequest;
import com.udea.FinanceTracker.dto.UsuarioDTO;
import com.udea.FinanceTracker.service.UsuarioService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AuthenticationController {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Google OAuth2 Login endpoint
     */
    @PostMapping("/google-login")
    public ResponseEntity<?> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
        try {
            logger.info("Google login request received");
            AuthenticationResponse response = usuarioService.authenticateWithGoogle(request);
            logger.info("Google login successful");
            return ResponseEntity.ok(response);
        } catch (GeneralSecurityException e) {
            logger.error("General security exception during Google login: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Google token verification failed");
            error.put("message", e.getMessage());
            error.put("errorType", "GeneralSecurityException");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (IOException e) {
            logger.error("IO exception during Google login: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to verify Google token");
            error.put("message", e.getMessage());
            error.put("errorType", "IOException");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (Exception e) {
            logger.error("Unexpected exception during Google login: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Authentication failed");
            error.put("message", e.getMessage());
            error.put("errorType", e.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Complete user profile (first time login)
     */
    @PostMapping("/complete-profile/{userId}")
    public ResponseEntity<?> completeProfile(
            @PathVariable Long userId,
            @Valid @RequestBody UpdatePerfilRequest request) {
        try {
            UsuarioDTO usuarioDTO = usuarioService.updateUserProfile(userId, request);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Perfil completado exitosamente");
            response.put("usuario", usuarioDTO);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al completar el perfil");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Validate JWT token
     */
    @PostMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractToken(authHeader);
            Boolean isValid = usuarioService.validateToken(token);

            if (isValid) {
                String email = usuarioService.getEmailFromToken(token);
                UsuarioDTO usuario = usuarioService.getUserByEmail(email);

                Map<String, Object> response = new HashMap<>();
                response.put("valid", true);
                response.put("email", email);
                response.put("usuario", usuario);
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Token inválido o expirado");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al validar el token");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    /**
     * Refresh access token
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(
            @RequestHeader("Authorization") String authHeader) {
        try {
            String refreshToken = extractToken(authHeader);
            String newAccessToken = usuarioService.refreshAccessToken(refreshToken);

            Map<String, String> response = new HashMap<>();
            response.put("accessToken", newAccessToken);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al refrescar el token");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    /**
     * Get current user profile
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractToken(authHeader);

            if (!usuarioService.validateToken(token)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Token inválido o expirado");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            String email = usuarioService.getEmailFromToken(token);
            UsuarioDTO usuario = usuarioService.getUserByEmail(email);

            if (usuario == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Usuario no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            return ResponseEntity.ok(usuario);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener el perfil");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Test login endpoint (for development/testing without real Google OAuth)
     * In production, remove this endpoint
     */
    @PostMapping("/test-login")
    public ResponseEntity<?> testLogin(@RequestParam String email, @RequestParam String name) {
        try {
            AuthenticationResponse response = usuarioService.createTestUser(email, name);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error en login de prueba");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Helper method to extract token from Authorization header
     */
    private String extractToken(String authHeader) throws Exception {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new Exception("Authorization header is missing or invalid");
        }
        return authHeader.substring(7);
    }
}

