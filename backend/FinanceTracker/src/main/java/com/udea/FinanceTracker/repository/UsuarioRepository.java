package com.udea.FinanceTracker.repository;

import com.udea.FinanceTracker.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByGoogleId(String googleId);
    Optional<Usuario> findByNumeroIdentificacion(Long numeroIdentificacion);
}

