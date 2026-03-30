package com.udea.FinanceTracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioDTO {
    private Long id;
    private String nombre;
    private String email;
    private String googleId;
    private Long idGenero;
    private String fechaNacimiento;
    private Long salario;
    private Long idOcupacion;
    private Boolean profileCompleted;
    private LocalDateTime createdAt;
}

