package com.udea.FinanceTracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Datos de gasto por categoría para el reporte")
public class CategoryExpenseDTO {

    @Schema(description = "Nombre de la categoría", example = "Alimentación")
    private String categoryName;

    @Schema(description = "Monto total gastado en la categoría", example = "150000.00")
    private BigDecimal amount;

    @Schema(description = "Porcentaje del gasto total", example = "35.5")
    private BigDecimal percentage;

    public CategoryExpenseDTO() {}

    public CategoryExpenseDTO(String categoryName, BigDecimal amount, BigDecimal percentage) {
        this.categoryName = categoryName;
        this.amount = amount;
        this.percentage = percentage;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getPercentage() {
        return percentage;
    }

    public void setPercentage(BigDecimal percentage) {
        this.percentage = percentage;
    }
}