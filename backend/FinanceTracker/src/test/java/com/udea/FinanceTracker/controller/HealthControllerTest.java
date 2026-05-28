package com.udea.FinanceTracker.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas unitarias para HealthController.
 * Valida los endpoints de salud de la aplicación.
 *
 * Patrón Triple AAA aplicado en cada prueba.
 *
 * @author Equipo Quality Assurance
 */
class HealthControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private HealthController healthController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(healthController).build();
    }

    /**
     * Prueba: Endpoint raíz retorna mensaje de bienvenida.
     *
     * Tipo de prueba: Funcional positivo (Sanity check)
     * Patrón AAA: Arrange, Act, Assert
     *
     * Corresponde a: Validación de que la API está disponible
     *
     * Resultado esperado: Código 200 OK, mensaje de bienvenida
     */
    @Test
    void root_ReturnsWelcomeMessage() throws Exception {
        // ==================== ACT & ASSERT ====================
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string("FinanceTracker API - Funcionando correctamente"));
    }

    /**
     * Prueba: Endpoint /health retorna estado de la aplicación.
     *
     * Tipo de prueba: Funcional positivo (Health check)
     * Patrón AAA: Arrange, Act, Assert
     *
     * Corresponde a: Validación de que la aplicación está saludable
     *
     * Resultado esperado: Código 200 OK, status UP, información de servicio
     */
    @Test
    void health_ReturnsServiceStatus() throws Exception {
        // ==================== ACT & ASSERT ====================
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("FinanceTracker"))
                .andExpect(jsonPath("$.message").value("Service is healthy"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    /**
     * Prueba: Endpoint /health retorna estructura JSON válida.
     *
     * Tipo de prueba: Funcional positivo (Validación de estructura)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void health_ReturnsValidJsonStructure() throws Exception {
        // ==================== ACT & ASSERT ====================
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").isString())
                .andExpect(jsonPath("$.service").isString())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.message").isString());
    }

    /**
     * Prueba: Endpoint /ping retorna "pong".
     *
     * Tipo de prueba: Funcional positivo (Ping/Pong check)
     * Patrón AAA: Arrange, Act, Assert
     *
     * Corresponde a: Validación de conectividad básica
     *
     * Resultado esperado: Código 200 OK, texto "pong"
     */
    @Test
    void ping_ReturnsPong() throws Exception {
        // ==================== ACT & ASSERT ====================
        mockMvc.perform(get("/ping"))
                .andExpect(status().isOk())
                .andExpect(content().string("pong"));
    }

    /**
     * Prueba: /health endpoint siempre retorna status UP.
     *
     * Tipo de prueba: Funcional positivo (Consistencia)
     * Patrón AAA: Arrange, Act, Assert
     *
     * Esta prueba valida que el estado es siempre consistente
     */
    @Test
    void health_AlwaysReturnsUpStatus() throws Exception {
        // ==================== ACT & ASSERT ====================
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(get("/health"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("UP"));
        }
    }
}

