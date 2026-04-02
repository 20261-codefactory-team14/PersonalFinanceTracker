package com.udea.FinanceTracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Petición para completar o actualizar el perfil del usuario")
public class UpdatePerfilRequest {

    @Schema(
            description = "Nombre completo del usuario",
            example = "Juan Pérez"
    )
    private String nombre;

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
            example = "2500000",
            minimum = "0"
    )
    private Long salario;

    @Schema(
            description = "ID de la ocupación del usuario",
            example = "3"
    )
    private Long idOcupacion;
}