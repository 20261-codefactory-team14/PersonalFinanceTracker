package com.udea.FinanceTracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "Reporte de distribución de gastos por categoría")
public class ExpenseDistributionReportDTO {

    @Schema(description = "Fecha de inicio del período", example = "2026-03-25")
    private LocalDate startDate;

    @Schema(description = "Fecha de fin del período", example = "2026-04-25")
    private LocalDate endDate;

    @Schema(description = "Monto total de gastos en el período", example = "423000.00")
    private BigDecimal totalExpenses;

    @Schema(description = "Lista de gastos por categoría")
    private List<CategoryExpenseDTO> categoryExpenses;

    public ExpenseDistributionReportDTO() {}

    public ExpenseDistributionReportDTO(LocalDate startDate, LocalDate endDate,
                                       BigDecimal totalExpenses, List<CategoryExpenseDTO> categoryExpenses) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalExpenses = totalExpenses;
        this.categoryExpenses = categoryExpenses;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(BigDecimal totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    public List<CategoryExpenseDTO> getCategoryExpenses() {
        return categoryExpenses;
    }

    public void setCategoryExpenses(List<CategoryExpenseDTO> categoryExpenses) {
        this.categoryExpenses = categoryExpenses;
    }
}

