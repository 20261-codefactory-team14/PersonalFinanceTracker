package com.udea.FinanceTracker.service;

import com.udea.FinanceTracker.dto.CategoryExpenseDTO;
import com.udea.FinanceTracker.dto.ExpenseDistributionReportDTO;
import com.udea.FinanceTracker.entity.Gasto;
import com.udea.FinanceTracker.entity.Presupuesto;
import com.udea.FinanceTracker.repository.GastoRepository;
import com.udea.FinanceTracker.repository.PresupuestoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExpenseReportService {

    private final GastoRepository gastoRepository;
    private final PresupuestoRepository presupuestoRepository;

    public ExpenseReportService(GastoRepository gastoRepository, PresupuestoRepository presupuestoRepository) {
        this.gastoRepository = gastoRepository;
        this.presupuestoRepository = presupuestoRepository;
    }

    /**
     * Genera un reporte de distribución de gastos por categoría para el período del último presupuesto completado.
     *
     * El campo 'fecha' del presupuesto es la fecha de INICIO del presupuesto.
     * El presupuesto dura 30 días, por lo que el período es: fecha a fecha+30días
     *
     * @param idUsuario ID del usuario
     * @return ExpenseDistributionReportDTO con los datos del reporte
     * @throws RuntimeException si no hay presupuestos completados
     */
    public ExpenseDistributionReportDTO generateDefaultReport(Long idUsuario) {
        LocalDate today = LocalDate.now();

        Presupuesto lastFinishedBudget = presupuestoRepository.findLastFinishedBudget(idUsuario, today)
                .orElseThrow(() -> new RuntimeException("No hay presupuestos completados para generar el reporte"));

        // La fecha del presupuesto es la fecha de INICIO
        LocalDate startDate = lastFinishedBudget.getFecha();
        // El período es de 30 días a partir de la fecha de inicio
        LocalDate endDate = startDate.plusDays(30);

        return generateReport(idUsuario, startDate, endDate);
    }

    /**
     * Genera un reporte de distribución de gastos por categoría para un rango de fechas personalizado.
     *
     * @param idUsuario ID del usuario
     * @param startDate Fecha de inicio del período
     * @param endDate Fecha de fin del período
     * @return ExpenseDistributionReportDTO con los datos del reporte
     */
    public ExpenseDistributionReportDTO generateCustomReport(Long idUsuario, LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new RuntimeException("La fecha de inicio debe ser menor o igual a la fecha de fin");
        }

        return generateReport(idUsuario, startDate, endDate);
    }

    /**
     * Método privado que realiza la generación del reporte.
     *
     * @param idUsuario ID del usuario
     * @param startDate Fecha de inicio del período
     * @param endDate Fecha de fin del período
     * @return ExpenseDistributionReportDTO con los datos del reporte
     */
    private ExpenseDistributionReportDTO generateReport(Long idUsuario, LocalDate startDate, LocalDate endDate) {
        // Obtener todos los gastos del usuario dentro del rango de fechas
        List<Gasto> gastos = gastoRepository.findGastosByUsuarioAndDateRange(idUsuario, startDate, endDate);

        if (gastos.isEmpty()) {
            return new ExpenseDistributionReportDTO(startDate, endDate, BigDecimal.ZERO, List.of());
        }

        // Calcular el total de gastos
        BigDecimal totalExpenses = gastos.stream()
                .map(Gasto::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Agrupar gastos por categoría y calcular totales
        Map<String, BigDecimal> categoryTotals = gastos.stream()
                .collect(Collectors.groupingBy(
                        gasto -> gasto.getCategoria().getNombre(),
                        Collectors.reducing(BigDecimal.ZERO, Gasto::getValor, BigDecimal::add)
                ));

        // Crear la lista de categorías con sus montos y porcentajes
        List<CategoryExpenseDTO> categoryExpenses = categoryTotals.entrySet().stream()
                .map(entry -> {
                    String categoryName = entry.getKey();
                    BigDecimal amount = entry.getValue();
                    BigDecimal percentage = amount.divide(totalExpenses, 2, RoundingMode.HALF_UP)
                            .multiply(new BigDecimal("100"));

                    return new CategoryExpenseDTO(categoryName, amount, percentage);
                })
                .sorted((a, b) -> b.getAmount().compareTo(a.getAmount())) // Ordenar por cantidad descendente
                .collect(Collectors.toList());

        return new ExpenseDistributionReportDTO(startDate, endDate, totalExpenses, categoryExpenses);
    }
}

