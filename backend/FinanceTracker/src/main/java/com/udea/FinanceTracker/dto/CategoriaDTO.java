package com.udea.FinanceTracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos de una categoría de gastos")
public class CategoriaDTO {

    @Schema(description = "ID de la categoría", example = "1")
    private Long id;

    @Schema(description = "Nombre de la categoría", example = "Alimentación")
    private String nombre;

    public CategoriaDTO() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}