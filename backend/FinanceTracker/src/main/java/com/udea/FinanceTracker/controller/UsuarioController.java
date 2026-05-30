package com.udea.FinanceTracker.controller;


import com.udea.FinanceTracker.service.UsuarioService;
import com.udea.FinanceTracker.dto.DeleteAccountRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("api/usuario")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /**
     * Delete user account permanently
     *
     * This endpoint:
     * 1. Requires valid JWT token in Authorization header
     * 2. Extracts userId directly from the JWT token
     * 3. Only the user can delete their own account
     * 4. Requires confirmation parameter set to true
     * 5. All related data (gasto, presupuesto, etc) auto-deleted via CASCADE
     * 6. JWT token immediately blacklisted to prevent further API calls
     *
     * Error responses:
     * - 400: Missing/invalid confirmation or JWT format issues
     * - 401: Missing or invalid/expired JWT token
     * - 403: Unauthorized access attempt
     * - 404: User not found
     * - 500: Server error during deletion
     *
     * @param authHeader Authorization header containing JWT token
     * @param request DeleteAccountRequest with confirmation flag
     * @return ResponseEntity with status and message
     */
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteAccount(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody DeleteAccountRequest request) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Validate authorization header exists
            if (authHeader == null || authHeader.trim().isEmpty()) {
                response.put("error", "Token de autenticación requerido");
                response.put("message", "Por favor proporcione un token JWT válido en el header Authorization");
                return ResponseEntity.status(401).body(response);
            }

            if (!authHeader.startsWith("Bearer ")) {
                response.put("error", "Formato de token inválido");
                response.put("message", "El token debe estar en formato 'Bearer <token>'");
                return ResponseEntity.status(400).body(response);
            }

            // Extract token
            String token = authHeader.substring(7).trim();

            if (token.isEmpty()) {
                response.put("error", "Token vacío");
                response.put("message", "El token JWT no puede estar vacío");
                return ResponseEntity.status(400).body(response);
            }

            // Validate confirmation parameter
            if (request.getConfirm() == null || !request.getConfirm()) {
                response.put("error", "Confirmación requerida");
                response.put("message", "Debes confirmar la eliminación de cuenta estableciendo 'confirm' en true");
                return ResponseEntity.status(400).body(response);
            }

            // Attempt to delete the user (token is validated and userId extracted inside)
            boolean deleted = usuarioService.deleteUser(token);

            if (deleted) {
                response.put("success", true);
                response.put("message", "Sesión finalizada");
                response.put("details", "Tu cuenta ha sido eliminada permanentemente junto con todos tus datos");
                return ResponseEntity.ok(response);
            } else {
                response.put("error", "Error inesperado");
                response.put("message", "No se pudo eliminar la cuenta");
                return ResponseEntity.status(500).body(response);
            }

        } catch (IllegalAccessException e) {
            // User attempting unauthorized action
            response.put("error", "Acceso denegado");
            response.put("message", e.getMessage());
            response.put("statusCode", 403);
            return ResponseEntity.status(403).body(response);

        } catch (IllegalArgumentException e) {
            // Token validation errors or missing/invalid user info
            String message = e.getMessage();

            // Determine appropriate HTTP status based on error type
            int status = 401; // Default to unauthorized

            if (message != null) {
                if (message.contains("expirado")) {
                    response.put("error", "Token expirado");
                    response.put("message", message);
                    status = 401;
                } else if (message.contains("malformado") || message.contains("corrupto") || message.contains("inválido")) {
                    response.put("error", "Token JWT inválido");
                    response.put("message", message);
                    status = 401;
                } else if (message.contains("no encontrado")) {
                    response.put("error", "Usuario no encontrado");
                    response.put("message", message);
                    status = 404;
                } else if (message.contains("Confirmación")) {
                    response.put("error", "Confirmación requerida");
                    response.put("message", message);
                    status = 400;
                } else {
                    response.put("error", "Solicitud inválida");
                    response.put("message", message);
                    status = 400;
                }
            } else {
                response.put("error", "Error de validación");
                response.put("message", "Error al procesar la solicitud");
                status = 400;
            }

            response.put("statusCode", status);
            return ResponseEntity.status(status).body(response);

        } catch (Exception e) {
            // Catch-all for unexpected errors
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Error desconocido";

            response.put("error", "Error al procesar la solicitud");
            response.put("message", errorMessage);
            response.put("statusCode", 500);

            return ResponseEntity.status(500).body(response);
        }
    }
}
