package com.udea.FinanceTracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Petición para crear una categoría de gastos")
public class CrearCategoriaRequest {

    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    @Schema(description = "Nombre de la categoría", example = "Alimentación", required = true)
    private String nombre;

    public CrearCategoriaRequest() {}

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}