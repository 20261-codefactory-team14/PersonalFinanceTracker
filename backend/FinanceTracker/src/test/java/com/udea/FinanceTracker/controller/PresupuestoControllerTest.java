package com.udea.FinanceTracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.udea.FinanceTracker.dto.CrearPresupuestoRequest;
import com.udea.FinanceTracker.dto.PresupuestoDTO;
import com.udea.FinanceTracker.service.PresupuestoService;
import com.udea.FinanceTracker.service.UsuarioService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de integración para PresupuestoController.
 * Valida los endpoints de presupuestos usando MockMvc.
 *
 * Patrón Triple AAA aplicado en cada prueba.
 *
 * @author Equipo Quality Assurance
 */
class PresupuestoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PresupuestoService presupuestoService;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private PresupuestoController presupuestoController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(presupuestoController).build();
        // Configurar ObjectMapper para soportar LocalDate
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Prueba del camino feliz: Establecer presupuesto global válido.
     *
     * Tipo de prueba: Funcional positivo (Camino Feliz)
     * Patrón AAA: Arrange, Act, Assert
     *
     * Corresponde al CP-007-A: Establecer presupuesto global válido
     */
    @Test
    void crearPresupuesto_WithValidMonto_ReturnsCreated() throws Exception {
        // ==================== ARRANGE ====================
        String authHeader = "Bearer valid_token";
        CrearPresupuestoRequest request = new CrearPresupuestoRequest();
        request.setValor(new BigDecimal("1500.00"));

        PresupuestoDTO response = new PresupuestoDTO();
        response.setId(1L);
        response.setValor(new BigDecimal("1500.00"));

        given(usuarioService.getEmailFromToken(any(String.class))).willReturn("user@example.com");
        given(usuarioService.getUserByEmail(any(String.class)))
                .willReturn(com.udea.FinanceTracker.dto.UsuarioDTO.builder().id(1L).build());
        given(presupuestoService.crearPresupuesto(eq(1L), any(CrearPresupuestoRequest.class)))
                .willReturn(response);

        // ==================== ACT & ASSERT ====================
        mockMvc.perform(post("/api/presupuesto")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valor").value(1500.00));
    }
}