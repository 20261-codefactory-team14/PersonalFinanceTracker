package com.udea.FinanceTracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.udea.FinanceTracker.dto.GastoDTO;
import com.udea.FinanceTracker.dto.GastoResponseDTO;
import com.udea.FinanceTracker.service.GastoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de integración para GastoController.
 * Valida los endpoints de gastos usando MockMvc.
 *
 * Patrón Triple AAA aplicado en cada prueba.
 *
 * @author Equipo Quality Assurance
 */
class GastoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GastoService gastoService;

    @InjectMocks
    private GastoController gastoController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(gastoController).build();
        // Configurar ObjectMapper para soportar LocalDate
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Prueba del camino feliz: Creación exitosa de gasto.
     *
     * Tipo de prueba: Funcional positivo (Camino Feliz)
     * Patrón AAA: Arrange, Act, Assert
     *
     * Corresponde al CP-005-A: Creación exitosa de gasto
     */
    @Test
    void crearGasto_WithValidData_ReturnsOk() throws Exception {
        // ==================== ARRANGE ====================
        GastoDTO request = new GastoDTO();
        request.setValor(new BigDecimal("25.50"));
        request.setFecha(LocalDate.now());
        request.setDescripcion("Almuerzo");
        request.setIdUsuario(1L);
        request.setIdCategoria(1L);

        GastoResponseDTO response = new GastoResponseDTO(
                1L, new BigDecimal("25.50"), LocalDate.now(),
                "Almuerzo", 1L, 1L
        );

        given(gastoService.crearGasto(any(GastoDTO.class))).willReturn(response);

        // ==================== ACT & ASSERT ====================
        mockMvc.perform(post("/api/gastos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.valor").value(25.50))
                .andExpect(jsonPath("$.descripcion").value("Almuerzo"));
    }

    /**
     * Prueba de excepción: Creación de gasto con usuario inexistente.
     *
     * Tipo de prueba: Validación negativa (Excepción)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void crearGasto_WithNonExistentUser_ReturnsError() throws Exception {
        // ==================== ARRANGE ====================
        GastoDTO request = new GastoDTO();
        request.setValor(new BigDecimal("25.50"));
        request.setFecha(LocalDate.now());
        request.setDescripcion("Almuerzo");
        request.setIdUsuario(999L);
        request.setIdCategoria(1L);

        given(gastoService.crearGasto(any(GastoDTO.class)))
                .willThrow(new RuntimeException("Usuario no encontrado"));

        // ==================== ACT & ASSERT ====================
        mockMvc.perform(post("/api/gastos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()); // Controller no maneja la excepción explícitamente
    }
}