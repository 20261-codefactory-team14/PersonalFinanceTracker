package com.udea.FinanceTracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Respuesta de datos de un gasto")
public class GastoResponseDTO {

    @Schema(description = "ID del gasto", example = "1")
    private Long id;

    @Schema(description = "Valor del gasto", example = "300.00")
    private BigDecimal valor;

    @Schema(description = "Fecha del gasto", example = "2023-01-01")
    private LocalDate fecha;

    @Schema(description = "Descripción del gasto", example = "Gastos de comida")
    private String descripcion;

    @Schema(description = "ID del usuario", example = "20")
    private Long idUsuario;

    @Schema(description = "ID de la categoría", example = "2")
    private Long idCategoria;

    public GastoResponseDTO(Long id, BigDecimal valor, LocalDate fecha,
                            String descripcion, Long idUsuario, Long idCategoria) {
        this.id = id;
        this.valor = valor;
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.idUsuario = idUsuario;
        this.idCategoria = idCategoria;
    }

    public Long getId() { return id; }
    public BigDecimal getValor() { return valor; }
    public LocalDate getFecha() { return fecha; }
    public String getDescripcion() { return descripcion; }
    public Long getIdUsuario() { return idUsuario; }
    public Long getIdCategoria() { return idCategoria; }
}