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

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    void crearIngreso_WithValidData_ReturnsOk() throws Exception {
        IngresoDTO request = new IngresoDTO();
        request.setValor(new BigDecimal("2000.00"));
        request.setFecha(LocalDate.now());
        request.setDescripcion("Salario Mensual");
        request.setIdUsuario(1L);
        request.setIdCategoria(2L);

        IngresoResponseDTO response = new IngresoResponseDTO(
                1L,
                new BigDecimal("2000.00"),
                LocalDate.now(),
                "Salario Mensual",
                1L,
                2L
        );

        given(ingresoService.crearIngreso(any(IngresoDTO.class))).willReturn(response);

        mockMvc.perform(post("/api/ingresos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.valor").value(2000.00))
                .andExpect(jsonPath("$.descripcion").value("Salario Mensual"))
                .andExpect(jsonPath("$.idUsuario").value(1))
                .andExpect(jsonPath("$.idCategoria").value(2));
    }

    @Test
    void crearIngreso_WithDecimalMonto_PreservesExactPrecision() throws Exception {
        IngresoDTO request = new IngresoDTO();
        request.setValor(new BigDecimal("0.45"));
        request.setFecha(LocalDate.now());
        request.setDescripcion("Intereses bancarios");
        request.setIdUsuario(1L);
        request.setIdCategoria(2L);

        IngresoResponseDTO response = new IngresoResponseDTO(
                1L,
                new BigDecimal("0.45"),
                LocalDate.now(),
                "Intereses bancarios",
                1L,
                2L
        );

        given(ingresoService.crearIngreso(any(IngresoDTO.class))).willReturn(response);

        mockMvc.perform(post("/api/ingresos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valor").value(0.45))
                .andExpect(jsonPath("$.valor").isNumber())
                .andExpect(jsonPath("$.idCategoria").value(2));

        String responseJson = mockMvc.perform(post("/api/ingresos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(responseJson).contains("\"valor\":0.45");
    }

    @Test
    void listarIngresos_ReturnsListOfIngresos() throws Exception {
        List<IngresoResponseDTO> ingresos = Arrays.asList(
                new IngresoResponseDTO(
                        1L,
                        new BigDecimal("2000.00"),
                        LocalDate.now(),
                        "Salario",
                        1L,
                        2L
                ),
                new IngresoResponseDTO(
                        2L,
                        new BigDecimal("500.00"),
                        LocalDate.now(),
                        "Bono",
                        1L,
                        3L
                )
        );

        given(ingresoService.listarIngresos()).willReturn(ingresos);

        mockMvc.perform(get("/api/ingresos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].valor").value(2000.00))
                .andExpect(jsonPath("$[0].idCategoria").value(2))
                .andExpect(jsonPath("$[1].valor").value(500.00))
                .andExpect(jsonPath("$[1].idCategoria").value(3));
    }

    @Test
    void obtenerIngresoPorId_WithValidId_ReturnsIngreso() throws Exception {
        Long ingresoId = 1L;

        IngresoResponseDTO response = new IngresoResponseDTO(
                ingresoId,
                new BigDecimal("2000.00"),
                LocalDate.now(),
                "Salario",
                1L,
                2L
        );

        given(ingresoService.obtenerIngresoPorId(ingresoId)).willReturn(response);

        mockMvc.perform(get("/api/ingresos/{id}", ingresoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.valor").value(2000.00))
                .andExpect(jsonPath("$.idCategoria").value(2));
    }

    @Test
    void obtenerIngresoPorId_WithInvalidId_ThrowsException() throws Exception {
        Long invalidId = 999L;

        given(ingresoService.obtenerIngresoPorId(invalidId))
                .willThrow(new RuntimeException("Ingreso no encontrado"));

        mockMvc.perform(get("/api/ingresos/{id}", invalidId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Ingreso no encontrado"));
    }

    @Test
    void listarIngresosPorUsuario_ReturnsIngresosForUser() throws Exception {
        Long usuarioId = 1L;

        List<IngresoResponseDTO> ingresos = Arrays.asList(
                new IngresoResponseDTO(
                        1L,
                        new BigDecimal("2000.00"),
                        LocalDate.now(),
                        "Salario",
                        usuarioId,
                        2L
                ),
                new IngresoResponseDTO(
                        2L,
                        new BigDecimal("500.00"),
                        LocalDate.now(),
                        "Bono",
                        usuarioId,
                        3L
                )
        );

        given(ingresoService.listarIngresosPorUsuario(usuarioId)).willReturn(ingresos);

        mockMvc.perform(get("/api/ingresos/usuario/{idUsuario}", usuarioId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].idCategoria").value(2))
                .andExpect(jsonPath("$[1].idCategoria").value(3));
    }

    @Test
    void actualizarIngreso_WithValidData_ReturnsUpdatedIngreso() throws Exception {
        Long ingresoId = 1L;

        IngresoDTO request = new IngresoDTO();
        request.setValor(new BigDecimal("2500.00"));
        request.setFecha(LocalDate.now());
        request.setDescripcion("Salario Actualizado");
        request.setIdUsuario(1L);
        request.setIdCategoria(2L);

        IngresoResponseDTO response = new IngresoResponseDTO(
                ingresoId,
                new BigDecimal("2500.00"),
                LocalDate.now(),
                "Salario Actualizado",
                1L,
                2L
        );

        given(ingresoService.actualizarIngreso(eq(ingresoId), any(IngresoDTO.class)))
                .willReturn(response);

        mockMvc.perform(put("/api/ingresos/{id}", ingresoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valor").value(2500.00))
                .andExpect(jsonPath("$.descripcion").value("Salario Actualizado"))
                .andExpect(jsonPath("$.idCategoria").value(2));
    }

    @Test
    void eliminarIngreso_WithValidId_ReturnsNoContent() throws Exception {
        Long ingresoId = 1L;

        doNothing().when(ingresoService).eliminarIngreso(ingresoId);

        mockMvc.perform(delete("/api/ingresos/{id}", ingresoId))
                .andExpect(status().isNoContent());
    }
}