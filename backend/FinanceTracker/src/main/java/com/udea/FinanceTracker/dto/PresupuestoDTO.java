package com.udea.FinanceTracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Datos del presupuesto mensual del usuario")
public class PresupuestoDTO {

    @Schema(description = "ID del presupuesto", example = "1")
    private Long id;

    @Schema(description = "Valor del presupuesto mensual", example = "2500000")
    private BigDecimal valor;

    @Schema(description = "Fecha de creación del presupuesto", example = "2026-04-01")
    private LocalDate fecha;

    @Schema(description = "Fecha de vencimiento del presupuesto (1 mes después de fecha)", example = "2026-05-01")
    private LocalDate fechaVencimiento;

    @Schema(description = "ID del usuario dueño del presupuesto", example = "12")
    private Long idUsuario;

    @Schema(description = "Indica si el presupuesto sigue vigente", example = "true")
    private Boolean activo;

    public PresupuestoDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}