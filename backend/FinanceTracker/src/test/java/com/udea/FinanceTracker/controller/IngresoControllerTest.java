package com.udea.FinanceTracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.udea.FinanceTracker.dto.IngresoDTO;
import com.udea.FinanceTracker.dto.IngresoResponseDTO;
import com.udea.FinanceTracker.service.IngresoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.assertj.core.api.Assertions.assertThat;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de integración para IngresoController.
 * Valida los endpoints de ingresos usando MockMvc.
 *
 * Tipo de prueba: Integración (MockMvc)
 * Patrón AAA: Arrange, Act, Assert
 *
 * @author Equipo Quality Assurance
 */
class IngresoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IngresoService ingresoService;

    @InjectMocks
    private IngresoController ingresoController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(ingresoController).build();
        // Configurar ObjectMapper para soportar LocalDate
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Prueba del camino feliz: Creación exitosa de ingreso.
     *
     * Tipo de prueba: Funcional positivo (Camino Feliz)
     * Patrón AAA: Arrange, Act, Assert
     *
     * Corresponde indirectamente al CP-004-B (registro de ingreso con decimales)
     */
    @Test
    void crearIngreso_WithValidData_ReturnsOk() throws Exception {
        // ==================== ARRANGE ====================
        IngresoDTO request = new IngresoDTO();
        request.setValor(new BigDecimal("2000.00"));
        request.setFecha(LocalDate.now());
        request.setDescripcion("Salario Mensual");
        request.setIdUsuario(1L);

        IngresoResponseDTO response = new IngresoResponseDTO(
                1L,
                new BigDecimal("2000.00"),
                LocalDate.now(),
                "Salario Mensual",
                1L
        );

        given(ingresoService.crearIngreso(any(IngresoDTO.class))).willReturn(response);

        // ==================== ACT & ASSERT ====================
        mockMvc.perform(post("/api/ingresos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.valor").value(2000.00))
                .andExpect(jsonPath("$.descripcion").value("Salario Mensual"))
                .andExpect(jsonPath("$.idUsuario").value(1));
    }

    /**
     * Prueba de precisión decimal: Registro de ingreso con centavos.
     *
     * Tipo de prueba: Funcional positivo (Precisión)
     * Patrón AAA: Arrange, Act, Assert
     *
     * Corresponde al CP-004-B: Soporte de decimales en registro de ingresos
     * Verifica que el sistema maneja correctamente montos con decimales (0.45)
     */
    @Test
    void crearIngreso_WithDecimalMonto_PreservesExactPrecision() throws Exception {
        // ==================== ARRANGE ====================
        IngresoDTO request = new IngresoDTO();
        request.setValor(new BigDecimal("0.45"));
        request.setFecha(LocalDate.now());
        request.setDescripcion("Intereses bancarios");
        request.setIdUsuario(1L);

        IngresoResponseDTO response = new IngresoResponseDTO(
                1L,
                new BigDecimal("0.45"),
                LocalDate.now(),
                "Intereses bancarios",
                1L
        );

        given(ingresoService.crearIngreso(any(IngresoDTO.class))).willReturn(response);

        // ==================== ACT & ASSERT ====================
        mockMvc.perform(post("/api/ingresos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valor").value(0.45))
                .andExpect(jsonPath("$.valor").isNumber());

        // Verificar que el valor no tiene pérdida de precisión (no debe ser 0.4499999999)
        String responseJson = mockMvc.perform(post("/api/ingresos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(responseJson).contains("\"valor\":0.45");
    }

    /**
     * Prueba: Listar todos los ingresos.
     *
     * Tipo de prueba: Funcional positivo (Consulta)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void listarIngresos_ReturnsListOfIngresos() throws Exception {
        // ==================== ARRANGE ====================
        List<IngresoResponseDTO> ingresos = Arrays.asList(
                new IngresoResponseDTO(1L, new BigDecimal("2000.00"), LocalDate.now(), "Salario", 1L),
                new IngresoResponseDTO(2L, new BigDecimal("500.00"), LocalDate.now(), "Bono", 1L)
        );

        given(ingresoService.listarIngresos()).willReturn(ingresos);

        // ==================== ACT & ASSERT ====================
        mockMvc.perform(get("/api/ingresos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].valor").value(2000.00))
                .andExpect(jsonPath("$[1].valor").value(500.00));
    }

    /**
     * Prueba: Obtener ingreso por ID exitosamente.
     *
     * Tipo de prueba: Funcional positivo (Consulta)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void obtenerIngresoPorId_WithValidId_ReturnsIngreso() throws Exception {
        // ==================== ARRANGE ====================
        Long ingresoId = 1L;
        IngresoResponseDTO response = new IngresoResponseDTO(
                ingresoId,
                new BigDecimal("2000.00"),
                LocalDate.now(),
                "Salario",
                1L
        );

        given(ingresoService.obtenerIngresoPorId(ingresoId)).willReturn(response);

        // ==================== ACT & ASSERT ====================
        mockMvc.perform(get("/api/ingresos/{id}", ingresoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.valor").value(2000.00));
    }

    /**
     * Prueba: Obtener ingreso por ID no existente.
     *
     * Tipo de prueba: Validación negativa (Excepción)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void obtenerIngresoPorId_WithInvalidId_ThrowsException() throws Exception {
        // ==================== ARRANGE ====================
        Long invalidId = 999L;

        given(ingresoService.obtenerIngresoPorId(invalidId))
                .willThrow(new RuntimeException("Ingreso no encontrado"));

        // ==================== ACT & ASSERT ====================
        mockMvc.perform(get("/api/ingresos/{id}", invalidId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Ingreso no encontrado"));  // validar mensaje
    }

    /**
     * Prueba: Listar ingresos por usuario.
     *
     * Tipo de prueba: Funcional positivo (Consulta)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void listarIngresosPorUsuario_ReturnsIngresosForUser() throws Exception {
        // ==================== ARRANGE ====================
        Long usuarioId = 1L;
        List<IngresoResponseDTO> ingresos = Arrays.asList(
                new IngresoResponseDTO(1L, new BigDecimal("2000.00"), LocalDate.now(), "Salario", usuarioId),
                new IngresoResponseDTO(2L, new BigDecimal("500.00"), LocalDate.now(), "Bono", usuarioId)
        );

        given(ingresoService.listarIngresosPorUsuario(usuarioId)).willReturn(ingresos);

        // ==================== ACT & ASSERT ====================
        mockMvc.perform(get("/api/ingresos/usuario/{idUsuario}", usuarioId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    /**
     * Prueba: Actualizar ingreso exitosamente.
     *
     * Tipo de prueba: Funcional positivo (Actualización)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void actualizarIngreso_WithValidData_ReturnsUpdatedIngreso() throws Exception {
        // ==================== ARRANGE ====================
        Long ingresoId = 1L;

        IngresoDTO request = new IngresoDTO();
        request.setValor(new BigDecimal("2500.00"));
        request.setFecha(LocalDate.now());
        request.setDescripcion("Salario Actualizado");
        request.setIdUsuario(1L);

        IngresoResponseDTO response = new IngresoResponseDTO(
                ingresoId,
                new BigDecimal("2500.00"),
                LocalDate.now(),
                "Salario Actualizado",
                1L
        );

        given(ingresoService.actualizarIngreso(eq(ingresoId), any(IngresoDTO.class)))
                .willReturn(response);

        // ==================== ACT & ASSERT ====================
        mockMvc.perform(put("/api/ingresos/{id}", ingresoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valor").value(2500.00))
                .andExpect(jsonPath("$.descripcion").value("Salario Actualizado"));
    }

    /**
     * Prueba: Eliminar ingreso exitosamente.
     *
     * Tipo de prueba: Funcional positivo (Eliminación)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void eliminarIngreso_WithValidId_ReturnsNoContent() throws Exception {
        // ==================== ARRANGE ====================
        Long ingresoId = 1L;

        doNothing().when(ingresoService).eliminarIngreso(ingresoId);

        // ==================== ACT & ASSERT ====================
        mockMvc.perform(delete("/api/ingresos/{id}", ingresoId))
                .andExpect(status().isNoContent());
    }
}