package com.udea.FinanceTracker.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "tbl_usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "googleId", unique = true)
    private String googleId;

    @Column(name = "idGenero")
    private Long idGenero;

    @Column(name = "fechaNacimiento")
    private Date fechaNacimiento;

    @Column(name = "numeroIdentificacion", unique = true)
    private Long numeroIdentificacion;

    @Column(name = "idTipoIdentificacion")
    private Long idTipoIdentificacion;

    @Column(name = "salario")
    private Long salario;

    @Column(name = "idOcupacion")
    private Long idOcupacion;

    @Column(name = "profileCompleted", nullable = false)
    private Boolean profileCompleted = false;

    @CreationTimestamp
    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    public Usuario() {
    }

    public Usuario(Long id, String nombre, String email, Long idGenero, Date fechaNacimiento, Long numeroIdentificacion, Long idTipoIdentificacion, Long salario, Long idOcupacion) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.idGenero = idGenero;
        this.fechaNacimiento = fechaNacimiento;
        this.numeroIdentificacion = numeroIdentificacion;
        this.idTipoIdentificacion = idTipoIdentificacion;
        this.salario = salario;
        this.idOcupacion = idOcupacion;
        this.profileCompleted = true;
    }

    public Usuario(String nombre, String email, String googleId) {
        this.nombre = nombre;
        this.email = email;
        this.googleId = googleId;
        this.profileCompleted = false;
    }

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getIdGenero() {
        return idGenero;
    }

    public void setIdGenero(Long idGenero) {
        this.idGenero = idGenero;
    }

    public Date getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(Date fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public Long getNumeroIdentificacion() {
        return numeroIdentificacion;
    }

    public void setNumeroIdentificacion(Long numeroIdentificacion) {
        this.numeroIdentificacion = numeroIdentificacion;
    }

    public Long getIdTipoIdentificacion() {
        return idTipoIdentificacion;
    }

    public void setIdTipoIdentificacion(Long idTipoIdentificacion) {
        this.idTipoIdentificacion = idTipoIdentificacion;
    }

    public Long getSalario() {
        return salario;
    }

    public void setSalario(Long salario) {
        this.salario = salario;
    }

    public Long getIdOcupacion() {
        return idOcupacion;
    }

    public void setIdOcupacion(Long idOcupacion) {
        this.idOcupacion = idOcupacion;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public Boolean getProfileCompleted() {
        return profileCompleted;
    }

    public void setProfileCompleted(Boolean profileCompleted) {
        this.profileCompleted = profileCompleted;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
