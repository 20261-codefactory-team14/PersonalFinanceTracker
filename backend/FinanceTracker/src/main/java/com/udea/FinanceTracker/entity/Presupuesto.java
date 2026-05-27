package com.udea.FinanceTracker.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tbl_presupuesto")
public class Presupuesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "valor", nullable = false)
    private BigDecimal valor;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    public Presupuesto() {}

    public Presupuesto(BigDecimal valor, LocalDate fecha, Long idUsuario) {
        this.valor = valor;
        this.fecha = fecha;
        this.idUsuario = idUsuario;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }
}