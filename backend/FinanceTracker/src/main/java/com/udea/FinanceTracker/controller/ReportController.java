package com.udea.FinanceTracker.controller;

import com.udea.FinanceTracker.dto.DateRangeReportRequest;
import com.udea.FinanceTracker.dto.ExpenseDistributionReportDTO;
import com.udea.FinanceTracker.service.ExpenseReportService;
import com.udea.FinanceTracker.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reportes")
@Tag(name = "Reportes", description = "Operaciones REST para generar reportes de gastos")
public class ReportController {

    private final ExpenseReportService expenseReportService;
    private final JwtUtil jwtUtil;

    public ReportController(ExpenseReportService expenseReportService, JwtUtil jwtUtil) {
        this.expenseReportService = expenseReportService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Genera un reporte de distribución de gastos por categoría para el período del último presupuesto completado.
     *
     * @param authHeader Authorization header con el JWT token
     * @return ExpenseDistributionReportDTO con los datos del reporte
     * @throws RuntimeException si no hay presupuestos completados o token inválido
     */
    @Operation(
        summary = "Generar reporte por defecto",
        description = "Genera un reporte de distribución de gastos por categoría usando el período del último presupuesto completado"
    )
    @GetMapping("/gastos-por-categoria")
    public ResponseEntity<ExpenseDistributionReportDTO> generateDefaultReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        // Validate and extract token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().build();
        }

        String token = authHeader.substring(7);
        Long userId = jwtUtil.extractUserId(token);

        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }

        ExpenseDistributionReportDTO report = expenseReportService.generateDefaultReport(userId);
        return ResponseEntity.ok(report);
    }

    /**
     * Genera un reporte de distribución de gastos por categoría para un rango de fechas personalizado.
     *
     * @param authHeader Authorization header con el JWT token
     * @param request DateRangeReportRequest con las fechas de inicio y fin
     * @return ExpenseDistributionReportDTO con los datos del reporte
     * @throws RuntimeException si las fechas son inválidas o token es inválido
     */
    @Operation(
        summary = "Generar reporte con rango personalizado",
        description = "Genera un reporte de distribución de gastos por categoría para un rango de fechas específico"
    )
    @PostMapping("/gastos-por-categoria")
    public ResponseEntity<ExpenseDistributionReportDTO> generateCustomReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody DateRangeReportRequest request
    ) {
        // Validate and extract token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().build();
        }

        String token = authHeader.substring(7);
        Long userId = jwtUtil.extractUserId(token);

        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }

        ExpenseDistributionReportDTO report = expenseReportService.generateCustomReport(
            userId,
            request.getStartDate(),
            request.getEndDate()
        );
        return ResponseEntity.ok(report);
    }
}

