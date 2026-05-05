package com.udea.FinanceTracker.repository;

import com.udea.FinanceTracker.entity.Categoria;
import com.udea.FinanceTracker.entity.Gasto;
import com.udea.FinanceTracker.entity.Usuario;
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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas de integración para GastoRepository.
 * Valida las consultas personalizadas del repositorio de gastos.
 *
 * Tipo de prueba: Integración (Base de datos real en memoria)
 * Patrón AAA: Arrange, Act, Assert
 *
 * @author Equipo Quality Assurance
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class GastoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private GastoRepository gastoRepository;

    private Usuario testUsuario;
    private Categoria testCategoria;
    private Gasto testGasto;

    @BeforeEach
    void setUp() {
        // ==================== ARRANGE ====================
        // Crear usuario de prueba
        testUsuario = new Usuario("Test User", "test@example.com", "google123");
        testUsuario = entityManager.persistAndFlush(testUsuario);

        // Crear categoría de prueba
        testCategoria = new Categoria("Alimentación");
        testCategoria = entityManager.persistAndFlush(testCategoria);

        // Crear gasto de prueba
        testGasto = new Gasto(
                new BigDecimal("25.50"),
                LocalDate.now(),
                "Almuerzo",
                testUsuario,
                testCategoria
        );
        testGasto = entityManager.persistAndFlush(testGasto);
    }

    /**
     * Prueba: Buscar gastos por ID de usuario.
     *
     * Tipo de prueba: Funcional positivo (Consulta)
     * Patrón AAA: Arrange, Act, Assert
     *
     * Corresponde indirectamente a CP-005-A (validar que el gasto se asocia correctamente al usuario)
     */
    @Test
    void findByUsuario_Id_ReturnsGastosForGivenUser() {
        // ==================== ACT ====================
        List<Gasto> gastos = gastoRepository.findByUsuario_Id(testUsuario.getId());

        // ==================== ASSERT ====================
        assertThat(gastos).isNotEmpty();
        assertThat(gastos).hasSize(1);
        assertThat(gastos.get(0).getUsuario().getId()).isEqualTo(testUsuario.getId());
        assertThat(gastos.get(0).getValor()).isEqualByComparingTo(new BigDecimal("25.50"));
        assertThat(gastos.get(0).getDescripcion()).isEqualTo("Almuerzo");
    }

    /**
     * Prueba: Buscar gastos por ID de categoría.
     *
     * Tipo de prueba: Funcional positivo (Consulta)
     * Patrón AAA: Arrange, Act, Assert
     *
     * Corresponde al CP-006-A (validar que el gasto se asocia correctamente a la categoría)
     */
    @Test
    void findByCategoria_Id_ReturnsGastosForGivenCategoria() {
        // ==================== ACT ====================
        List<Gasto> gastos = gastoRepository.findByCategoria_Id(testCategoria.getId());

        // ==================== ASSERT ====================
        assertThat(gastos).isNotEmpty();
        assertThat(gastos).hasSize(1);
        assertThat(gastos.get(0).getCategoria().getId()).isEqualTo(testCategoria.getId());
        assertThat(gastos.get(0).getCategoria().getNombre()).isEqualTo("Alimentación");
    }

    /**
     * Prueba: Buscar gastos por ID de usuario - usuario sin gastos.
     *
     * Tipo de prueba: Validación (Borde)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void findByUsuario_Id_WithNoGastos_ReturnsEmptyList() {
        // ==================== ARRANGE ====================
        Usuario usuarioSinGastos = new Usuario("Empty User", "empty@example.com", "google456");
        usuarioSinGastos = entityManager.persistAndFlush(usuarioSinGastos);

        // ==================== ACT ====================
        List<Gasto> gastos = gastoRepository.findByUsuario_Id(usuarioSinGastos.getId());

        // ==================== ASSERT ====================
        assertThat(gastos).isEmpty();
    }

    /**
     * Prueba: Buscar gastos por ID de categoría - categoría sin gastos.
     *
     * Tipo de prueba: Validación (Borde)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void findByCategoria_Id_WithNoGastos_ReturnsEmptyList() {
        // ==================== ARRANGE ====================
        Categoria categoriaSinGastos = new Categoria("Transporte");
        categoriaSinGastos = entityManager.persistAndFlush(categoriaSinGastos);

        // ==================== ACT ====================
        List<Gasto> gastos = gastoRepository.findByCategoria_Id(categoriaSinGastos.getId());

        // ==================== ASSERT ====================
        assertThat(gastos).isEmpty();
    }

    /**
     * Prueba: Verificar que el gasto guardado tiene todos los campos correctos.
     *
     * Tipo de prueba: Funcional positivo (Persistencia)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void savedGasto_HasAllFieldsCorrectly() {
        // ==================== ARRANGE ====================
        BigDecimal nuevoValor = new BigDecimal("100.00");
        LocalDate nuevaFecha = LocalDate.of(2025, 5, 15);
        String nuevaDescripcion = "Cena con amigos";

        Gasto nuevoGasto = new Gasto(
                nuevoValor,
                nuevaFecha,
                nuevaDescripcion,
                testUsuario,
                testCategoria
        );

        // ==================== ACT ====================
        Gasto gastoGuardado = entityManager.persistAndFlush(nuevoGasto);

        // ==================== ASSERT ====================
        assertThat(gastoGuardado.getId()).isNotNull();
        assertThat(gastoGuardado.getValor()).isEqualByComparingTo(nuevoValor);
        assertThat(gastoGuardado.getFecha()).isEqualTo(nuevaFecha);
        assertThat(gastoGuardado.getDescripcion()).isEqualTo(nuevaDescripcion);
        assertThat(gastoGuardado.getUsuario().getId()).isEqualTo(testUsuario.getId());
        assertThat(gastoGuardado.getCategoria().getId()).isEqualTo(testCategoria.getId());
    }
}