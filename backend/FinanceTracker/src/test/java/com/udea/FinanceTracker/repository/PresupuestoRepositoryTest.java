package com.udea.FinanceTracker.repository;

import com.udea.FinanceTracker.entity.Presupuesto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas de integración para PresupuestoRepository.
 * Valida las consultas personalizadas del repositorio de presupuestos.
 *
 * Tipo de prueba: Integración (Base de datos real en memoria)
 * Patrón AAA: Arrange, Act, Assert
 *
 * @author Equipo Quality Assurance
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PresupuestoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PresupuestoRepository presupuestoRepository;

    private static final Long USUARIO_ID = 1L;
    private static final Long USUARIO_ID_2 = 2L;

    @BeforeEach
    void setUp() {
        // ==================== ARRANGE ====================
        // Crear presupuesto activo (fecha dentro del último mes)
        Presupuesto presupuestoActivo = new Presupuesto(
                new BigDecimal("1500.00"),
                LocalDate.now().minusDays(5), // Fecha reciente
                USUARIO_ID
        );
        entityManager.persistAndFlush(presupuestoActivo);

        // Crear presupuesto expirado (fecha hace más de un mes)
        Presupuesto presupuestoExpirado = new Presupuesto(
                new BigDecimal("1000.00"),
                LocalDate.now().minusMonths(2), // Fecha antigua
                USUARIO_ID
        );
        entityManager.persistAndFlush(presupuestoExpirado);

        // Crear presupuesto para otro usuario
        Presupuesto presupuestoOtroUsuario = new Presupuesto(
                new BigDecimal("2000.00"),
                LocalDate.now().minusDays(10),
                USUARIO_ID_2
        );
        entityManager.persistAndFlush(presupuestoOtroUsuario);
    }

    /**
     * Prueba: Buscar presupuesto activo del usuario (fecha > fecha límite).
     *
     * Tipo de prueba: Funcional positivo (Consulta)
     * Patrón AAA: Arrange, Act, Assert
     *
     * Corresponde al CP-007-A (validar que se consulta el presupuesto activo correctamente)
     */
    @Test
    void findPresupuestoActivoByUsuario_ReturnsActiveBudget() {
        // ==================== ARRANGE ====================
        LocalDate fechaLimite = LocalDate.now().minusMonths(1);

        // ==================== ACT ====================
        Optional<Presupuesto> resultado = presupuestoRepository
                .findPresupuestoActivoByUsuario(USUARIO_ID, fechaLimite);

        // ==================== ASSERT ====================
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getValor()).isEqualByComparingTo(new BigDecimal("1500.00"));
        assertThat(resultado.get().getFecha()).isAfter(fechaLimite);
    }

    /**
     * Prueba: Buscar presupuesto activo - usuario sin presupuesto activo.
     *
     * Tipo de prueba: Validación (Borde)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void findPresupuestoActivoByUsuario_WithNoActiveBudget_ReturnsEmpty() {
        // ==================== ARRANGE ====================
        Long usuarioSinPresupuesto = 999L;
        LocalDate fechaLimite = LocalDate.now().minusMonths(1);

        // ==================== ACT ====================
        Optional<Presupuesto> resultado = presupuestoRepository
                .findPresupuestoActivoByUsuario(usuarioSinPresupuesto, fechaLimite);

        // ==================== ASSERT ====================
        assertThat(resultado).isEmpty();
    }

    /**
     * Prueba: Verificar si el usuario tiene presupuesto activo.
     *
     * Tipo de prueba: Funcional positivo (Consulta booleana)
     * Patrón AAA: Arrange, Act, Assert
     *
     * Corresponde al CP-007-A (validar existencia de presupuesto activo)
     */
    @Test
    void existsPresupuestoActivoByUsuario_ReturnsTrueWhenActiveBudgetExists() {
        // ==================== ARRANGE ====================
        LocalDate fechaLimite = LocalDate.now().minusMonths(1);

        // ==================== ACT ====================
        Boolean existe = presupuestoRepository
                .existsPresupuestoActivoByUsuario(USUARIO_ID, fechaLimite);

        // ==================== ASSERT ====================
        assertThat(existe).isTrue();
    }

    /**
     * Prueba: Verificar si el usuario tiene presupuesto activo - cuando no tiene.
     *
     * Tipo de prueba: Validación (Borde)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void existsPresupuestoActivoByUsuario_ReturnsFalseWhenNoActiveBudget() {
        // ==================== ARRANGE ====================
        Long usuarioSinPresupuesto = 999L;
        LocalDate fechaLimite = LocalDate.now().minusMonths(1);

        // ==================== ACT ====================
        Boolean existe = presupuestoRepository
                .existsPresupuestoActivoByUsuario(usuarioSinPresupuesto, fechaLimite);

        // ==================== ASSERT ====================
        assertThat(existe).isFalse();
    }

    /**
     * Prueba: Obtener todos los presupuestos de un usuario ordenados por fecha descendente.
     *
     * Tipo de prueba: Funcional positivo (Consulta)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void findByIdUsuarioOrderByFechaDesc_ReturnsAllBudgetsForUserOrderedByDate() {
        // ==================== ACT ====================
        List<Presupuesto> presupuestos = presupuestoRepository
                .findByIdUsuarioOrderByFechaDesc(USUARIO_ID);

        // ==================== ASSERT ====================
        assertThat(presupuestos).hasSize(2);
        // Verificar orden descendente (más reciente primero)
        assertThat(presupuestos.get(0).getFecha())
                .isAfterOrEqualTo(presupuestos.get(1).getFecha());
    }

    /**
     * Prueba: Obtener todos los presupuestos - usuario sin presupuestos.
     *
     * Tipo de prueba: Validación (Borde)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void findByIdUsuarioOrderByFechaDesc_WithNoBudgets_ReturnsEmptyList() {
        // ==================== ARRANGE ====================
        Long usuarioSinPresupuesto = 888L;

        // ==================== ACT ====================
        List<Presupuesto> presupuestos = presupuestoRepository
                .findByIdUsuarioOrderByFechaDesc(usuarioSinPresupuesto);

        // ==================== ASSERT ====================
        assertThat(presupuestos).isEmpty();
    }
}