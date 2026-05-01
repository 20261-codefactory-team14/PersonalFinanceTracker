package com.udea.FinanceTracker.repository;

import com.udea.FinanceTracker.entity.Gasto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GastoRepository extends JpaRepository<Gasto, Long> {

    List<Gasto> findByUsuario_Id(Long idUsuario);

    List<Gasto> findByCategoria_Id(Long idCategoria);
}