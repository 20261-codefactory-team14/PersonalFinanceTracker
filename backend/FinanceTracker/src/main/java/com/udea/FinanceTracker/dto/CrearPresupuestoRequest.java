package com.udea.FinanceTracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "Petición para crear un presupuesto mensual")
public class CrearPresupuestoRequest {

    @NotNull(message = "El valor del presupuesto es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El valor debe ser mayor a 0")
    @Schema(description = "Valor del presupuesto mensual", example = "2500000", required = true)
    private BigDecimal valor;

    public CrearPresupuestoRequest() {}

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
}