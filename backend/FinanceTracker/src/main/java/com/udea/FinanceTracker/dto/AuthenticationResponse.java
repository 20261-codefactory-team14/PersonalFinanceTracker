package com.udea.FinanceTracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Respuesta de autenticación del sistema, contiene tokens y datos del usuario")
public class AuthenticationResponse {

    @Schema(
            description = "Token de acceso JWT utilizado para autenticación en endpoints protegidos",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    )
    private String accessToken;

    @Schema(
            description = "Token de refresco utilizado para generar nuevos access tokens",
            example = "dGhpc0lzQVJlZnJlc2hUb2tlbg=="
    )
    private String refreshToken;

    @Schema(
            description = "Indica si el usuario ya completó su perfil",
            example = "true"
    )
    private Boolean profileCompleted;

    @Schema(
            description = "Información del usuario autenticado"
    )
    private UsuarioDTO usuario;

    @Schema(
            description = "Mensaje adicional de respuesta (opcional)",
            example = "Login exitoso"
    )
    private String message;
}