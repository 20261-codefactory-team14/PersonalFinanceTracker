package com.udea.FinanceTracker.repository;

import com.udea.FinanceTracker.entity.Gasto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/** TODO AGREGAR CATEGORIA */
public interface GastoRepository extends JpaRepository<Gasto, Long> {

    List<Gasto> findByUsuarioId(Long idUsuario);
    // List<Gasto> findByCategoriaId(Long idCategoria);
}