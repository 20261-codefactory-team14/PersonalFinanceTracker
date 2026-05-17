package com.udea.FinanceTracker.repository;

import com.udea.FinanceTracker.entity.Gasto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface GastoRepository extends JpaRepository<Gasto, Long> {

    List<Gasto> findByUsuario_Id(Long idUsuario);

    List<Gasto> findByCategoria_Id(Long idCategoria);

    // Busca gastos de un usuario dentro de un rango de fechas
    @Query("SELECT g FROM Gasto g WHERE g.usuario.id = :idUsuario AND g.fecha >= :startDate AND g.fecha <= :endDate ORDER BY g.fecha DESC")
    List<Gasto> findGastosByUsuarioAndDateRange(
            @Param("idUsuario") Long idUsuario,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // Busca gastos de un usuario después de una fecha específica (para usar con presupuestos activos)
    @Query("SELECT g FROM Gasto g WHERE g.usuario.id = :idUsuario AND g.fecha > :startDate ORDER BY g.fecha DESC")
    List<Gasto> findGastosByUsuarioAndAfterDate(
            @Param("idUsuario") Long idUsuario,
            @Param("startDate") LocalDate startDate
    );
}