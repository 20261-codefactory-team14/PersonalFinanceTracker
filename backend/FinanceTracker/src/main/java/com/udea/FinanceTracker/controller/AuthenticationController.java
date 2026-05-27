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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Tag(name = "Autenticación", description = "Endpoints para autenticación con Google, validación de JWT, refresco de token y gestión básica del perfil del usuario")
public class AuthenticationController {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Google OAuth2 Login endpoint
     */
    @Operation(
            summary = "Iniciar sesión o registrarse con Google",
            description = "Recibe el token de Google en el cuerpo de la petición, lo valida y retorna la respuesta de autenticación del sistema, incluyendo los tokens y la información básica del usuario."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Autenticación exitosa",
                    content = @Content(
                            mediaType = "application/json"
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "El token de Google es inválido o no pudo verificarse",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "Error de verificación", value = """
                                    {
                                      "error": "Google token verification failed",
                                      "message": "Invalid token",
                                      "errorType": "GeneralSecurityException"
                                    }
                                    """),
                                    @ExampleObject(name = "Error IO", value = """
                                    {
                                      "error": "Failed to verify Google token",
                                      "message": "I/O error",
                                      "errorType": "IOException"
                                    }
                                    """)
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno durante la autenticación",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                            {
                              "error": "Authentication failed",
                              "message": "Unexpected error",
                              "errorType": "RuntimeException"
                            }
                            """)
                    )
            )
    })
    @PostMapping("/google-login")
    public ResponseEntity<?> googleLogin(

        @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Token de Google enviado por el cliente para autenticar al usuario",
                required = true,
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = GoogleLoginRequest.class),
                        examples = @ExampleObject(value = """
                            {
                              "idToken": "eyJhbGciOiJSUzI1NiIsImtpZCI6..."
                            }
                            """)
                )
        )
        @Valid @RequestBody GoogleLoginRequest request) {

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
    @Operation(
            summary = "Completar perfil del usuario",
            description = "Permite completar o actualizar el perfil inicial de un usuario después del login, usando el id del usuario como parámetro de ruta.",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Perfil completado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                        {
                          "message": "Perfil completado exitosamente",
                          "usuario": {
                            "id": 1,
                            "nombre": "Juan Pérez",
                            "email": "juan@gmail.com",
                            "idOcupacion": 3,
                            "salario": 2500000
                          }
                        }
                        """)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No tienes permiso para modificar este perfil",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                        {
                          "error": "No tienes permiso para modificar este perfil"
                        }
                        """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Error al completar el perfil o datos inválidos",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                        {
                          "error": "Error al completar el perfil",
                          "message": "El usuario no existe"
                        }
                        """)
                    )
            )
    })
    @PostMapping("/complete-profile/{userId}")
    public ResponseEntity<?> completeProfile(
            @Parameter(description = "ID del usuario al que se le completará el perfil", example = "1", required = true)
            @PathVariable Long userId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos necesarios para completar o actualizar el perfil del usuario",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UpdatePerfilRequest.class),
                            examples = @ExampleObject(value = """
                        {
                          "nombre": "Juan Pérez",
                          "idGenero": 1,
                          "fechaNacimiento": "2000-01-01",
                          "salario": 2500000,
                          "idOcupacion": 2
                        }
                        """)
                    )
            )
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody UpdatePerfilRequest request) {
        try {
            String token = authHeader.substring(7);
            String emailDelToken = usuarioService.getEmailFromToken(token);
            UsuarioDTO usuarioAutenticado = usuarioService.getUserByEmail(emailDelToken);

            if (!usuarioAutenticado.getId().equals(userId)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "No tienes permiso para modificar este perfil");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

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

    @Operation(
            summary = "Validar token JWT",
            description = "Valida el access token enviado en el header Authorization con el formato Bearer. Si es válido, retorna el correo y la información del usuario.",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token válido",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                            {
                              "valid": true,
                              "email": "juan@gmail.com",
                              "usuario": {
                                "id": 1,
                                "nombre": "Juan Pérez",
                                "email": "juan@gmail.com"
                              }
                            }
                            """)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token inválido, expirado o header incorrecto",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "Token inválido", value = """
                                    {
                                      "error": "Token inválido o expirado"
                                    }
                                    """),
                                    @ExampleObject(name = "Header inválido", value = """
                                    {
                                      "error": "Error al validar el token",
                                      "message": "Authorization header is missing or invalid"
                                    }
                                    """)
                            }
                    )
            )
    })
    @PostMapping("/validate-token")
    public ResponseEntity<?> validateToken(
        @Parameter(
                description = "Token JWT con prefijo Bearer",
                example = "Bearer eyJhbGciOiJIUzI1NiJ9..."
        )
        @RequestHeader("Authorization") String authHeader) {
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

    @Operation(
            summary = "Refrescar access token",
            description = "Recibe un refresh token en el header Authorization con formato Bearer y retorna un nuevo access token.",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Nuevo access token generado correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                            {
                              "accessToken": "nuevo_jwt_access_token"
                            }
                            """)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Refresh token inválido, expirado o header incorrecto",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                            {
                              "error": "Error al refrescar el token",
                              "message": "Refresh token inválido"
                            }
                            """)
                    )
            )
    })
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(
            @Parameter(
                    description = "Refresh token con prefijo Bearer",
                    example = "Bearer eyJhbGciOiJIUzI1NiJ9..."
            )
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

    @Operation(
            summary = "Obtener perfil del usuario autenticado",
            description = "Retorna la información del usuario asociado al token JWT enviado en el header Authorization.",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Perfil obtenido exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UsuarioDTO.class),
                            examples = @ExampleObject(value = """
                            {
                              "id": 1,
                              "nombre": "Juan Pérez",
                              "email": "juan@gmail.com",
                              "idOcupacion": 3,
                              "salario": 2500000
                            }
                            """)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token inválido o expirado",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                            {
                              "error": "Token inválido o expirado"
                            }
                            """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                            {
                              "error": "Usuario no encontrado"
                            }
                            """)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno al obtener el perfil",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                            {
                              "error": "Error al obtener el perfil",
                              "message": "Unexpected error"
                            }
                            """)
                    )
            )
    })
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(

            @Parameter(
                    description = "Token JWT con prefijo Bearer",
                    example = "Bearer eyJhbGciOiJIUzI1NiJ9..."
            )
            @RequestHeader("Authorization") String authHeader) {
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

    /**
     * Logout user
     */
    @Operation(
            summary = "Cerrar sesión",
            description = "Invalida el access token enviado en Authorization y, si se envía, también el refresh token. En una app sin frontend, el cliente debe eliminar los tokens almacenados.",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Sesión cerrada exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                            {
                              "success": true,
                              "message": "Sesión cerrada correctamente"
                            }
                            """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Token inválido o faltante",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                            {
                              "error": "Authorization header is missing or invalid"
                            }
                            """)
                    )
            )
    })
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody(required = false) Map<String, String> body) {
        try {
            String accessToken = extractToken(authHeader);
            String refreshToken = body != null ? body.get("refreshToken") : null;

            usuarioService.logout(accessToken, refreshToken);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Sesión cerrada correctamente");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}

