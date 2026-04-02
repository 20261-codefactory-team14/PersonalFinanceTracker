package com.udea.FinanceTracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Petición para autenticación con Google OAuth2")
public class GoogleLoginRequest {

    @NotBlank(message = "Google token is required")
    @JsonProperty("idToken")
    @Schema(
            description = "Token ID proporcionado por Google tras autenticación del usuario",
            example = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjE2Yz..."
    )
    private String idToken;
}