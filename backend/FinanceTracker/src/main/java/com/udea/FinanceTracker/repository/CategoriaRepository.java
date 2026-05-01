package com.udea.FinanceTracker.repository;

import com.udea.FinanceTracker.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    List<Categoria> findAllByOrderByNombreAsc();

    Boolean existsByNombreIgnoreCase(String nombre);

    Boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}