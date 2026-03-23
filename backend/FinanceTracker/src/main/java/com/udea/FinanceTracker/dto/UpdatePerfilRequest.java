package com.udea.FinanceTracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePerfilRequest {
    @NotBlank(message = "Gender ID is required")
    private Long idGenero;

    @NotBlank(message = "Birth date is required")
    private String fechaNacimiento;

    @NotBlank(message = "Identification number is required")
    private Long numeroIdentificacion;

    @NotBlank(message = "Identification type ID is required")
    private Long idTipoIdentificacion;

    private Long salario;

    private Long idOcupacion;
}

