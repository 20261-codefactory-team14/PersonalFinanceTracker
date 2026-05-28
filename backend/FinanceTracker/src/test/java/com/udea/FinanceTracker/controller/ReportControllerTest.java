package com.udea.FinanceTracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.udea.FinanceTracker.dto.CategoryExpenseDTO;
import com.udea.FinanceTracker.dto.DateRangeReportRequest;
import com.udea.FinanceTracker.dto.ExpenseDistributionReportDTO;
import com.udea.FinanceTracker.service.ExpenseReportService;
import com.udea.FinanceTracker.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas unitarias para ReportController.
 * Valida los endpoints de generación de reportes.
 *
 * Patrón Triple AAA aplicado en cada prueba.
 *
 * @author Equipo Quality Assurance
 */
class ReportControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ExpenseReportService expenseReportService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private ReportController reportController;

    private ObjectMapper objectMapper;

    private static final String VALID_JWT_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIn0.valid";
    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(reportController).build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Prueba del camino feliz: Generación exitosa de reporte por defecto.
     *
     * Tipo de prueba: Funcional positivo (Camino feliz)
     * Patrón AAA: Arrange, Act, Assert
     *
     * Corresponde a: Caso de uso de generación de reporte por período predefinido
     *
     * Escenario: Usuario autenticado, token válido, reporte generado exitosamente
     * Resultado esperado: Código 200 OK, reporte con datos
     */
    @Test
    void generateDefaultReport_WithValidToken_ReturnsExpenseDistribution() throws Exception {
        // ==================== ARRANGE ====================
        ExpenseDistributionReportDTO report = new ExpenseDistributionReportDTO(
                LocalDate.now().minusDays(30),
                LocalDate.now(),
                new BigDecimal("400000"),
                Arrays.asList(
                        new CategoryExpenseDTO("Alimentación", new BigDecimal("150000"), new BigDecimal("35.0")),
                        new CategoryExpenseDTO("Transporte", new BigDecimal("80000"), new BigDecimal("18.0")),
                        new CategoryExpenseDTO("Entretenimiento", new BigDecimal("170000"), new BigDecimal("40.0"))
                )
        );

        given(jwtUtil.extractUserId(VALID_JWT_TOKEN)).willReturn(USER_ID);
        given(expenseReportService.generateDefaultReport(USER_ID)).willReturn(report);

        // ==================== ACT & ASSERT ====================
        mockMvc.perform(get("/api/reportes/gastos-por-categoria")
                        .header("Authorization", "Bearer " + VALID_JWT_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalExpenses").value(400000))
                .andExpect(jsonPath("$.categoryExpenses.length()").value(3))
                .andExpect(jsonPath("$.categoryExpenses[0].categoryName").value("Alimentación"));
    }

    /**
     * Prueba de excepción: Generación de reporte sin Authorization header.
     *
     * Tipo de prueba: Validación negativa (Autenticación faltante)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void generateDefaultReport_WithoutAuthorizationHeader_ReturnsBadRequest() throws Exception {
        // ==================== ARRANGE ====================
        // No Authorization header provided

        // ==================== ACT & ASSERT ====================
        mockMvc.perform(get("/api/reportes/gastos-por-categoria"))
                .andExpect(status().isBadRequest());
    }

    /**
     * Prueba de excepción: Generación de reporte con token inválido.
     *
     * Tipo de prueba: Validación negativa (Token inválido)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void generateDefaultReport_WithInvalidToken_ReturnsBadRequest() throws Exception {
        // ==================== ARRANGE ====================
        String invalidToken = "invalid-token";

        given(jwtUtil.extractUserId(invalidToken)).willReturn(null);

        // ==================== ACT & ASSERT ====================
        mockMvc.perform(get("/api/reportes/gastos-por-categoria")
                        .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isBadRequest());
    }

    /**
     * Prueba de excepción: Generación de reporte con formato Authorization inválido.
     *
     * Tipo de prueba: Validación negativa (Formato de Authorization inválido)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void generateDefaultReport_WithInvalidAuthorizationFormat_ReturnsBadRequest() throws Exception {
        // ==================== ARRANGE ====================
        String invalidFormat = "InvalidFormat " + VALID_JWT_TOKEN;

        // ==================== ACT & ASSERT ====================
        mockMvc.perform(get("/api/reportes/gastos-por-categoria")
                        .header("Authorization", invalidFormat))
                .andExpect(status().isBadRequest());
    }

    /**
     * Prueba del camino feliz: Generación exitosa de reporte con rango personalizado.
     *
     * Tipo de prueba: Funcional positivo (Camino feliz)
     * Patrón AAA: Arrange, Act, Assert
     *
     * Corresponde a: Caso de uso de generación de reporte con rango de fechas personalizado
     */
    @Test
    void generateCustomReport_WithValidTokenAndDateRange_ReturnsExpenseDistribution() throws Exception {
        // ==================== ARRANGE ====================
        DateRangeReportRequest request = new DateRangeReportRequest();
        request.setStartDate(LocalDate.of(2026, 1, 1));
        request.setEndDate(LocalDate.of(2026, 3, 31));

        ExpenseDistributionReportDTO report = new ExpenseDistributionReportDTO(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 3, 31),
                new BigDecimal("1250000"),
                Arrays.asList(
                        new CategoryExpenseDTO("Alimentación", new BigDecimal("500000"), new BigDecimal("40.0")),
                        new CategoryExpenseDTO("Vivienda", new BigDecimal("750000"), new BigDecimal("60.0"))
                )
        );

        given(jwtUtil.extractUserId(VALID_JWT_TOKEN)).willReturn(USER_ID);
        given(expenseReportService.generateCustomReport(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .willReturn(report);

        // ==================== ACT & ASSERT ====================
        mockMvc.perform(post("/api/reportes/gastos-por-categoria")
                        .header("Authorization", "Bearer " + VALID_JWT_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalExpenses").value(1250000))
                .andExpect(jsonPath("$.categoryExpenses.length()").value(2));
    }

    /**
     * Prueba de excepción: Generación de reporte con rango personalizado sin Authorization.
     *
     * Tipo de prueba: Validación negativa (Autenticación faltante)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void generateCustomReport_WithoutAuthorizationHeader_ReturnsBadRequest() throws Exception {
        // ==================== ARRANGE ====================
        DateRangeReportRequest request = new DateRangeReportRequest();
        request.setStartDate(LocalDate.of(2026, 1, 1));
        request.setEndDate(LocalDate.of(2026, 3, 31));

        // ==================== ACT & ASSERT ====================
        mockMvc.perform(post("/api/reportes/gastos-por-categoria")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Prueba de excepción: Generación de reporte con token inválido en rango personalizado.
     *
     * Tipo de prueba: Validación negativa (Token inválido)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void generateCustomReport_WithInvalidToken_ReturnsBadRequest() throws Exception {
        // ==================== ARRANGE ====================
        DateRangeReportRequest request = new DateRangeReportRequest();
        request.setStartDate(LocalDate.of(2026, 1, 1));
        request.setEndDate(LocalDate.of(2026, 3, 31));

        String invalidToken = "invalid-token";
        given(jwtUtil.extractUserId(invalidToken)).willReturn(null);

        // ==================== ACT & ASSERT ====================
        mockMvc.perform(post("/api/reportes/gastos-por-categoria")
                        .header("Authorization", "Bearer " + invalidToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Prueba: Generación de reporte con fecha de inicio posterior a fecha de fin.
     *
     * Tipo de prueba: Validación negativa (Rango de fechas inválido)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void generateCustomReport_WithInvalidDateRange_StillReturnsReport() throws Exception {
        // ==================== ARRANGE ====================
        DateRangeReportRequest request = new DateRangeReportRequest();
        request.setStartDate(LocalDate.of(2026, 3, 31));
        request.setEndDate(LocalDate.of(2026, 1, 1)); // End before start

        // Controller doesn't validate date range, service does - returns empty report
        ExpenseDistributionReportDTO report = new ExpenseDistributionReportDTO(
                LocalDate.of(2026, 3, 31),
                LocalDate.of(2026, 1, 1),
                BigDecimal.ZERO,
                Arrays.asList()
        );

        given(jwtUtil.extractUserId(VALID_JWT_TOKEN)).willReturn(USER_ID);
        given(expenseReportService.generateCustomReport(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .willReturn(report);

        // ==================== ACT & ASSERT ====================
        mockMvc.perform(post("/api/reportes/gastos-por-categoria")
                        .header("Authorization", "Bearer " + VALID_JWT_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    /**
     * Prueba: Generación de reporte devuelve lista vacía de categorías.
     *
     * Tipo de prueba: Validación positiva (Sin gastos en el período)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void generateDefaultReport_WithNoExpenses_ReturnsEmptyCategoryList() throws Exception {
        // ==================== ARRANGE ====================
        ExpenseDistributionReportDTO report = new ExpenseDistributionReportDTO(
                LocalDate.now().minusDays(30),
                LocalDate.now(),
                BigDecimal.ZERO,
                Arrays.asList()
        );

        given(jwtUtil.extractUserId(VALID_JWT_TOKEN)).willReturn(USER_ID);
        given(expenseReportService.generateDefaultReport(USER_ID)).willReturn(report);

        // ==================== ACT & ASSERT ====================
        mockMvc.perform(get("/api/reportes/gastos-por-categoria")
                        .header("Authorization", "Bearer " + VALID_JWT_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryExpenses.length()").value(0))
                .andExpect(jsonPath("$.totalExpenses").value(0));
    }
}

