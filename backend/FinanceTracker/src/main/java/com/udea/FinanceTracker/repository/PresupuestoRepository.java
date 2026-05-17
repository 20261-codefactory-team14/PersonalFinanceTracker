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

    // Busca el último presupuesto completado (fecha + 30 días debe ser menor a hoy)
    // El campo 'fecha' es la fecha de inicio del presupuesto, por lo que se suma 30 días para obtener la fecha de vencimiento
    @Query("SELECT p FROM Presupuesto p WHERE p.idUsuario = :idUsuario AND DATE_ADD(p.fecha, INTERVAL 30 DAY) < :today ORDER BY p.fecha DESC LIMIT 1")
    Optional<Presupuesto> findLastFinishedBudget(
            @Param("idUsuario") Long idUsuario,
            @Param("today") LocalDate today
    );
}