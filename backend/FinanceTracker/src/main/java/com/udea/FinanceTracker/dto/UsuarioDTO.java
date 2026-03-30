package com.udea.FinanceTracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Representa la información del usuario dentro del sistema")
public class UsuarioDTO {

    @Schema(
            description = "ID único del usuario",
            example = "1"
    )
    private Long id;

    @Schema(
            description = "Nombre completo del usuario",
            example = "Juan Pérez"
    )
    private String nombre;

    @Schema(
            description = "Correo electrónico del usuario",
            example = "juan.perez@gmail.com"
    )
    private String email;

    @Schema(
            description = "ID único proporcionado por Google para autenticación",
            example = "113245678901234567890"
    )
    private String googleId;

    @Schema(
            description = "ID del género del usuario",
            example = "1",
            nullable = true
    )
    private Long idGenero;

    @Schema(
            description = "Fecha de nacimiento del usuario en formato YYYY-MM-DD",
            example = "1998-05-21"
    )
    private String fechaNacimiento;

    @Schema(
            description = "Salario mensual del usuario",
            example = "2500000"
    )
    private Long salario;

    @Schema(
            description = "ID de la ocupación del usuario",
            example = "3"
    )
    private Long idOcupacion;

    @Schema(
            description = "Indica si el usuario ya completó su perfil",
            example = "true"
    )
    private Boolean profileCompleted;

    @Schema(
            description = "Fecha de creación del usuario en el sistema",
            example = "2026-03-29T14:30:00"
    )
    private LocalDateTime createdAt;
}