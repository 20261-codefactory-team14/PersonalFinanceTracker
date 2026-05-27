package com.udea.FinanceTracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class IngresoResponseDTO {

    private Long id;
    private BigDecimal valor;
    private LocalDate fecha;
    private String descripcion;
    private Long idUsuario;

    public IngresoResponseDTO(Long id, BigDecimal valor, LocalDate fecha, String descripcion, Long idUsuario) {
        this.id = id;
        this.valor = valor;
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.idUsuario = idUsuario;
    }

    public Long getId() { return id; }
    public BigDecimal getValor() { return valor; }
    public LocalDate getFecha() { return fecha; }
    public String getDescripcion() { return descripcion; }
    public Long getIdUsuario() { return idUsuario; }
}