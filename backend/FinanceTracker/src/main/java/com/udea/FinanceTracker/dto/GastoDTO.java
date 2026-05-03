package com.udea.FinanceTracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Datos para gastos")
public class GastoDTO {

    @Schema(description = "Valor del gasto", example = "1000.00")
    private BigDecimal valor;

    @Schema(description = "Fecha del gasto", example = "2023-01-01")
    private LocalDate fecha;

    @Schema(description = "Descripción", example = "Gastos de comida")
    private String descripcion;

    @Schema(description = "ID del usuario", example = "1")
    private Long idUsuario;

    @Schema(description = "ID de la categoría", example = "2")
    private Long idCategoria;

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }

    public Long getIdCategoria() { return idCategoria; }
    public void setIdCategoria(Long idCategoria) { this.idCategoria = idCategoria; }
}