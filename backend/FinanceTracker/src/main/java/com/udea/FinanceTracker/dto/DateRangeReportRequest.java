package com.udea.FinanceTracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "Solicitud para generar reporte con rango de fechas personalizado")
public class DateRangeReportRequest {

    @Schema(description = "Fecha de inicio del período", example = "2026-02-25", required = true)
    private LocalDate startDate;

    @Schema(description = "Fecha de fin del período", example = "2026-04-25", required = true)
    private LocalDate endDate;

    public DateRangeReportRequest() {}

    public DateRangeReportRequest(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
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
}


