package com.udea.FinanceTracker.repository;

import com.udea.FinanceTracker.entity.Presupuesto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface PresupuestoRepository extends JpaRepository<Presupuesto, Long> {

    // Busca el presupuesto activo del usuario (fecha de vencimiento mayor a hoy)
    @Query("SELECT p FROM Presupuesto p WHERE p.idUsuario = :idUsuario AND p.fecha > :fechaLimite")
    Optional<Presupuesto> findPresupuestoActivoByUsuario(
            @Param("idUsuario") Long idUsuario,
            @Param("fechaLimite") LocalDate fechaLimite
    );

    // Verifica si el usuario ya tiene un presupuesto activo
    @Query("SELECT COUNT(p) > 0 FROM Presupuesto p WHERE p.idUsuario = :idUsuario AND p.fecha > :fechaLimite")
    Boolean existsPresupuestoActivoByUsuario(
            @Param("idUsuario") Long idUsuario,
            @Param("fechaLimite") LocalDate fechaLimite
    );

    // Todos los presupuestos de un usuario
    List<Presupuesto> findByIdUsuarioOrderByFechaDesc(Long idUsuario);
}