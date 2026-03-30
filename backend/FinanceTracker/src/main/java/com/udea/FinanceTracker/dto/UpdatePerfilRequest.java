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
    private Long idGenero;

    private String fechaNacimiento;

    private Long salario;

    private Long idOcupacion;
}

