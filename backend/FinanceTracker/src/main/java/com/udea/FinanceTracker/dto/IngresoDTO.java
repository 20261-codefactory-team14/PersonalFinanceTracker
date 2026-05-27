package com.udea.FinanceTracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Datos para ingresos")
public class IngresoDTO {

    @Schema(description = "Valor del ingreso", example = "1000.00")
    private BigDecimal valor;

    @Schema(description = "Fecha del ingreso", example = "2023-01-01")
    private LocalDate fecha;

    @Schema(description = "Descripción", example = "Pago de servicios")
    private String descripcion;

    @Schema(description = "ID del usuario", example = "1")
    private Long idUsuario;

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }
}