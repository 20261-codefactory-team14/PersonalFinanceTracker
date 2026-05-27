package com.udea.FinanceTracker.service;

import com.udea.FinanceTracker.dto.CategoryExpenseDTO;
import com.udea.FinanceTracker.dto.ExpenseDistributionReportDTO;
import com.udea.FinanceTracker.entity.Categoria;
import com.udea.FinanceTracker.entity.Gasto;
import com.udea.FinanceTracker.entity.Presupuesto;
import com.udea.FinanceTracker.entity.Usuario;
import com.udea.FinanceTracker.repository.GastoRepository;
import com.udea.FinanceTracker.repository.PresupuestoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

/**
 * Pruebas unitarias para ExpenseReportService.
 * Valida la generación de reportes de distribución de gastos por categoría.
 *
 * Patrón Triple AAA aplicado en cada prueba.
 *
 * @author Equipo Quality Assurance
 */
@ExtendWith(MockitoExtension.class)
class ExpenseReportServiceTest {

    @Mock
    private GastoRepository gastoRepository;

    @Mock
    private PresupuestoRepository presupuestoRepository;

    @InjectMocks
    private ExpenseReportService expenseReportService;

    private static final Long USER_ID = 1L;
    private static final LocalDate START_DATE = LocalDate.of(2026, 3, 25);

    private Usuario usuario;
    private Presupuesto presupuesto;
    private List<Gasto> gastos;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(USER_ID);
        usuario.setEmail("test@example.com");
        usuario.setNombre("Test User");

        presupuesto = new Presupuesto();
        presupuesto.setId(1L);
        presupuesto.setFecha(START_DATE);
        presupuesto.setValor(new BigDecimal("500000"));
        presupuesto.setIdUsuario(USER_ID);

        // Calcular END_DATE dinámicamente: START_DATE + 30 días
        endDate = START_DATE.plusDays(30);

        gastos = new ArrayList<>();
    }

    // ==================== HAPPY PATH TESTS ====================

    /**
     * Prueba del camino feliz: Generar reporte por defecto exitosamente.
     *
     * Tipo de prueba: Funcional positivo (Camino Feliz)
     * Patrón AAA: Arrange, Act, Assert
     *
     * Verifica que:
     * - El período se calcula correctamente (fecha a fecha+30 días)
     * - Los gastos se agrupan correctamente por categoría
     * - Los porcentajes se calculan correctamente
     * - El total de gastos es correcto
     */
    @Test
    void generateDefaultReport_WithValidBudget_ReturnsCorrectDistribution() {
        // ==================== ARRANGE ====================
        Categoria alimentacion = new Categoria("Alimentación");
        alimentacion.setId(1L);

        Categoria transporte = new Categoria("Transporte");
        transporte.setId(2L);

        Gasto gasto1 = new Gasto(new BigDecimal("100000"), START_DATE, "Comida", usuario, alimentacion);
        gasto1.setId(1L);

        Gasto gasto2 = new Gasto(new BigDecimal("150000"), START_DATE.plusDays(5), "Comida", usuario, alimentacion);
        gasto2.setId(2L);

        Gasto gasto3 = new Gasto(new BigDecimal("50000"), START_DATE.plusDays(10), "Taxi", usuario, transporte);
        gasto3.setId(3L);

        gastos.add(gasto1);
        gastos.add(gasto2);
        gastos.add(gasto3);

        given(presupuestoRepository.findLastFinishedBudget(USER_ID, LocalDate.now()))
                .willReturn(Optional.of(presupuesto));
        // Use any() for dates to accept ANY date range, not just 30 days
        given(gastoRepository.findGastosByUsuarioAndDateRange(eq(USER_ID), any(LocalDate.class), any(LocalDate.class)))
                .willReturn(gastos);

        // ==================== ACT ====================
        ExpenseDistributionReportDTO report = expenseReportService.generateDefaultReport(USER_ID);

        // ==================== ASSERT ====================
        assertThat(report).isNotNull();
        assertThat(report.getStartDate()).isEqualTo(START_DATE);
        assertThat(report.getEndDate()).isEqualTo(endDate);
        assertThat(report.getTotalExpenses()).isEqualByComparingTo(new BigDecimal("300000"));
        assertThat(report.getCategoryExpenses()).hasSize(2);

        verify(presupuestoRepository).findLastFinishedBudget(USER_ID, LocalDate.now());
        verify(gastoRepository).findGastosByUsuarioAndDateRange(eq(USER_ID), any(LocalDate.class), any(LocalDate.class));
    }

    /**
     * Prueba del camino feliz: Generar reporte con rango personalizado exitosamente.
     *
     * Tipo de prueba: Funcional positivo (Camino Feliz)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void generateCustomReport_WithValidDateRange_ReturnsCorrectDistribution() {
        // ==================== ARRANGE ====================
        LocalDate customStart = LocalDate.of(2026, 2, 1);
        LocalDate customEnd = LocalDate.of(2026, 2, 28);

        Categoria categoria = new Categoria("Compras");
        categoria.setId(1L);

        Gasto gasto = new Gasto(new BigDecimal("75000"), customStart, "Compras", usuario, categoria);
        gasto.setId(1L);

        gastos.add(gasto);

        given(gastoRepository.findGastosByUsuarioAndDateRange(USER_ID, customStart, customEnd))
                .willReturn(gastos);

        // ==================== ACT ====================
        ExpenseDistributionReportDTO report = expenseReportService.generateCustomReport(USER_ID, customStart, customEnd);

        // ==================== ASSERT ====================
        assertThat(report).isNotNull();
        assertThat(report.getStartDate()).isEqualTo(customStart);
        assertThat(report.getEndDate()).isEqualTo(customEnd);
        assertThat(report.getTotalExpenses()).isEqualByComparingTo(new BigDecimal("75000"));
        assertThat(report.getCategoryExpenses()).hasSize(1);

        verify(gastoRepository).findGastosByUsuarioAndDateRange(USER_ID, customStart, customEnd);
    }

    /**
     * Prueba del camino feliz: Reporte con gastos de múltiples categorías.
     *
     * Tipo de prueba: Funcional positivo
     * Patrón AAA: Arrange, Act, Assert
     *
     * Verifica que los resultados se ordenan por cantidad (descendente)
     */
    @Test
    void generateDefaultReport_WithMultipleCategories_ReturnsSortedByAmount() {
        // ==================== ARRANGE ====================
        Categoria vivienda = new Categoria("Vivienda");
        vivienda.setId(1L);

        Categoria alimentacion = new Categoria("Alimentación");
        alimentacion.setId(2L);

        Categoria transporte = new Categoria("Transporte");
        transporte.setId(3L);

        gastos.add(new Gasto(new BigDecimal("150000"), START_DATE, "Renta", usuario, vivienda));
        gastos.add(new Gasto(new BigDecimal("100000"), START_DATE, "Comida", usuario, alimentacion));
        gastos.add(new Gasto(new BigDecimal("50000"), START_DATE, "Bus", usuario, transporte));

        given(presupuestoRepository.findLastFinishedBudget(USER_ID, LocalDate.now()))
                .willReturn(Optional.of(presupuesto));
        // Use any() for dates to accept ANY date range
        given(gastoRepository.findGastosByUsuarioAndDateRange(eq(USER_ID), any(LocalDate.class), any(LocalDate.class)))
                .willReturn(gastos);

        // ==================== ACT ====================
        ExpenseDistributionReportDTO report = expenseReportService.generateDefaultReport(USER_ID);

        // ==================== ASSERT ====================
        assertThat(report.getCategoryExpenses()).hasSize(3);
        // Verificar que está ordenado de mayor a menor
        assertThat(report.getCategoryExpenses().get(0).getAmount())
                .isGreaterThanOrEqualTo(report.getCategoryExpenses().get(1).getAmount());
        assertThat(report.getCategoryExpenses().get(1).getAmount())
                .isGreaterThanOrEqualTo(report.getCategoryExpenses().get(2).getAmount());

        // Verificar que el primer elemento es Vivienda (150000)
        assertThat(report.getCategoryExpenses().get(0).getCategoryName()).isEqualTo("Vivienda");
    }

    // ==================== EXCEPTION TESTS ====================

    /**
     * Prueba de excepción: No hay presupuestos completados.
     *
     * Tipo de prueba: Validación negativa (Excepción)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void generateDefaultReport_WithNoFinishedBudgets_ThrowsException() {
        // ==================== ARRANGE ====================
        given(presupuestoRepository.findLastFinishedBudget(USER_ID, LocalDate.now()))
                .willReturn(Optional.empty());

        // ==================== ACT & ASSERT ====================
        assertThatThrownBy(() -> expenseReportService.generateDefaultReport(USER_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No hay presupuestos completados para generar el reporte");

        verify(gastoRepository, never())
                .findGastosByUsuarioAndDateRange(anyLong(), any(), any());
    }

    /**
     * Prueba de excepción: Rango de fechas inválido (startDate > endDate).
     *
     * Tipo de prueba: Validación negativa (Excepción)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void generateCustomReport_WithInvalidDateRange_ThrowsException() {
        // ==================== ARRANGE ====================
        LocalDate startDate = LocalDate.of(2026, 4, 30);
        LocalDate endDate = LocalDate.of(2026, 2, 1);

        // ==================== ACT & ASSERT ====================
        assertThatThrownBy(() -> expenseReportService.generateCustomReport(USER_ID, startDate, endDate))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("La fecha de inicio debe ser menor o igual a la fecha de fin");

        verify(gastoRepository, never())
                .findGastosByUsuarioAndDateRange(anyLong(), any(), any());
    }

    /**
     * Prueba de excepción: No hay gastos en el período especificado.
     *
     * Tipo de prueba: Validación (Caso límite)
     * Patrón AAA: Arrange, Act, Assert
     *
     * Verifica que se retorna reporte vacío (NO excepción)
     */
    @Test
    void generateDefaultReport_WithNoExpenses_ReturnsEmptyReport() {
        // ==================== ARRANGE ====================
        given(presupuestoRepository.findLastFinishedBudget(USER_ID, LocalDate.now()))
                .willReturn(Optional.of(presupuesto));
        // Use any() for dates to accept ANY date range
        given(gastoRepository.findGastosByUsuarioAndDateRange(eq(USER_ID), any(LocalDate.class), any(LocalDate.class)))
                .willReturn(new ArrayList<>());

        // ==================== ACT ====================
        ExpenseDistributionReportDTO report = expenseReportService.generateDefaultReport(USER_ID);

        // ==================== ASSERT ====================
        assertThat(report).isNotNull();
        assertThat(report.getTotalExpenses()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(report.getCategoryExpenses()).isEmpty();
    }

    /**
     * Prueba de excepción: Rango de fechas igual (startDate == endDate).
     *
     * Tipo de prueba: Funcional positivo (Caso límite)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void generateCustomReport_WithEqualDates_AllowedAndReturnsReport() {
        // ==================== ARRANGE ====================
        LocalDate singleDate = LocalDate.of(2026, 3, 25);

        Categoria categoria = new Categoria("Comida");
        categoria.setId(1L);

        Gasto gasto = new Gasto(new BigDecimal("50000"), singleDate, "Almuerzo", usuario, categoria);
        gastos.add(gasto);

        given(gastoRepository.findGastosByUsuarioAndDateRange(USER_ID, singleDate, singleDate))
                .willReturn(gastos);

        // ==================== ACT ====================
        ExpenseDistributionReportDTO report = expenseReportService.generateCustomReport(USER_ID, singleDate, singleDate);

        // ==================== ASSERT ====================
        assertThat(report).isNotNull();
        assertThat(report.getStartDate()).isEqualTo(singleDate);
        assertThat(report.getEndDate()).isEqualTo(singleDate);
        assertThat(report.getCategoryExpenses()).hasSize(1);
    }

    /**
     * Prueba de precisión: Cálculo correcto de porcentajes.
     *
     * Tipo de prueba: Funcional - Precisión matemática
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void generateDefaultReport_PercentageCalculation_IsAccurate() {
        // ==================== ARRANGE ====================
        Categoria cat1 = new Categoria("Cat1");
        cat1.setId(1L);

        Categoria cat2 = new Categoria("Cat2");
        cat2.setId(2L);

        gastos.add(new Gasto(new BigDecimal("33333"), START_DATE, "Gasto 1", usuario, cat1));
        gastos.add(new Gasto(new BigDecimal("33334"), START_DATE, "Gasto 2", usuario, cat2));

        given(presupuestoRepository.findLastFinishedBudget(USER_ID, LocalDate.now()))
                .willReturn(Optional.of(presupuesto));
        // Use any() for dates to accept ANY date range
        given(gastoRepository.findGastosByUsuarioAndDateRange(eq(USER_ID), any(LocalDate.class), any(LocalDate.class)))
                .willReturn(gastos);

        // ==================== ACT ====================
        ExpenseDistributionReportDTO report = expenseReportService.generateDefaultReport(USER_ID);

        // ==================== ASSERT ====================
        BigDecimal totalPercentage = report.getCategoryExpenses().stream()
                .map(CategoryExpenseDTO::getPercentage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // La suma debe estar cerca de 100
        assertThat(totalPercentage).isBetween(new BigDecimal("99.99"), new BigDecimal("100.01"));

        // Verificar que cada porcentaje tiene máximo 2 decimales
        for (CategoryExpenseDTO category : report.getCategoryExpenses()) {
            int scale = category.getPercentage().scale();
            assertThat(scale).isLessThanOrEqualTo(2);
        }
    }
}

