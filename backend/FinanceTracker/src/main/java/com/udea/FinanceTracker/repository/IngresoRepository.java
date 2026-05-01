package com.udea.FinanceTracker.repository;

import com.udea.FinanceTracker.entity.Ingreso;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IngresoRepository extends JpaRepository<Ingreso, Long> {

    List<Ingreso> findByUsuario_Id(Long usuarioId);
}
