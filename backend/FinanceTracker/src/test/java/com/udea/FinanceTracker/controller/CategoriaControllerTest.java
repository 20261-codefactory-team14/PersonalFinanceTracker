package com.udea.FinanceTracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.udea.FinanceTracker.dto.ActualizarCategoriaRequest;
import com.udea.FinanceTracker.dto.CategoriaDTO;
import com.udea.FinanceTracker.dto.CrearCategoriaRequest;
import com.udea.FinanceTracker.service.CategoriaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas unitarias para CategoriaController.
 * Valida los endpoints de gestión de categorías.
 *
 * Patrón Triple AAA aplicado en cada prueba.
 *
 * @author Equipo Quality Assurance
 */
class CategoriaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CategoriaService categoriaService;

    @InjectMocks
    private CategoriaController categoriaController;

    private ObjectMapper objectMapper;

    private static final String AUTH_HEADER = "Bearer valid-token";
    private static final Long CATEGORY_ID = 1L;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(categoriaController).build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Prueba del camino feliz: Creación exitosa de categoría.
     *
     * Tipo de prueba: Funcional positivo (Camino feliz)
     * Patrón AAA: Arrange, Act, Assert
     *
     * Corresponde a: Caso de uso de creación de categoría exitosa
     *
     * Escenario: Usuario autenticado, datos válidos
     * Resultado esperado: Código 201 Created, categoría creada
     */
    @Test
    void crearCategoria_WithValidData_ReturnsCreated() throws Exception {
        // ==================== ARRANGE ====================
        CrearCategoriaRequest request = new CrearCategoriaRequest();
        request.setNombre("Alimentación");

        CategoriaDTO response = new CategoriaDTO();
        response.setId(CATEGORY_ID);
        response.setNombre("Alimentación");

        given(categoriaService.crearCategoria(any(CrearCategoriaRequest.class))).willReturn(response);

        // ==================== ACT & ASSERT ====================
        mockMvc.perform(post("/api/categories")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(CATEGORY_ID))
                .andExpect(jsonPath("$.nombre").value("Alimentación"));
    }

    /**
     * Prueba de excepción: Creación con datos inválidos.
     *
     * Tipo de prueba: Validación negativa (Datos inválidos)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void crearCategoria_WithInvalidData_ReturnsBadRequest() throws Exception {
        // ==================== ARRANGE ====================
        CrearCategoriaRequest request = new CrearCategoriaRequest();
        // Nombre vacío o nulo simularía datos inválidos

        given(categoriaService.crearCategoria(any(CrearCategoriaRequest.class)))
                .willThrow(new IllegalArgumentException("El nombre de la categoría es requerido"));

        // ==================== ACT & ASSERT ====================
        mockMvc.perform(post("/api/categories")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Prueba del camino feliz: Actualización exitosa de categoría.
     *
     * Tipo de prueba: Funcional positivo (Camino feliz)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void actualizarCategoria_WithValidData_ReturnsOk() throws Exception {
        // ==================== ARRANGE ====================
        ActualizarCategoriaRequest request = new ActualizarCategoriaRequest();
        request.setNombre("Alimentación Actualizada");

        CategoriaDTO response = new CategoriaDTO();
        response.setId(CATEGORY_ID);
        response.setNombre("Alimentación Actualizada");

        given(categoriaService.actualizarCategoria(eq(CATEGORY_ID), any(ActualizarCategoriaRequest.class)))
                .willReturn(response);

        // ==================== ACT & ASSERT ====================
        mockMvc.perform(put("/api/categories/{categoryId}", CATEGORY_ID)
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Alimentación Actualizada"));
    }

    /**
     * Prueba de excepción: Actualización de categoría inexistente.
     *
     * Tipo de prueba: Validación negativa (Recurso no encontrado)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void actualizarCategoria_WithNonexistentId_ReturnsBadRequest() throws Exception {
        // ==================== ARRANGE ====================
        Long nonexistentId = 999L;
        ActualizarCategoriaRequest request = new ActualizarCategoriaRequest();
        request.setNombre("Test");

        given(categoriaService.actualizarCategoria(eq(nonexistentId), any(ActualizarCategoriaRequest.class)))
                .willThrow(new RuntimeException("Categoría no encontrada"));

        // ==================== ACT & ASSERT ====================
        mockMvc.perform(put("/api/categories/{categoryId}", nonexistentId)
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Prueba del camino feliz: Obtención exitosa de todas las categorías.
     *
     * Tipo de prueba: Funcional positivo (Camino feliz)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void obtenerCategorias_ReturnsListOfCategories() throws Exception {
        // ==================== ARRANGE ====================
        List<CategoriaDTO> categorias = Arrays.asList();
        CategoriaDTO cat1 = new CategoriaDTO();
        cat1.setId(1L);
        cat1.setNombre("Alimentación");

        CategoriaDTO cat2 = new CategoriaDTO();
        cat2.setId(2L);
        cat2.setNombre("Transporte");

        CategoriaDTO cat3 = new CategoriaDTO();
        cat3.setId(3L);
        cat3.setNombre("Entretenimiento");

        categorias = Arrays.asList(cat1, cat2, cat3);

        given(categoriaService.obtenerCategorias()).willReturn(categorias);

        // ==================== ACT & ASSERT ====================
        mockMvc.perform(get("/api/categories")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].nombre").value("Alimentación"))
                .andExpect(jsonPath("$[1].nombre").value("Transporte"))
                .andExpect(jsonPath("$[2].nombre").value("Entretenimiento"));
    }

    /**
     * Prueba: Obtención de categorías devuelve lista vacía.
     *
     * Tipo de prueba: Validación positiva (Lista vacía)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void obtenerCategorias_WithNoCategories_ReturnsEmptyList() throws Exception {
        // ==================== ARRANGE ====================
        given(categoriaService.obtenerCategorias()).willReturn(Arrays.asList());

        // ==================== ACT & ASSERT ====================
        mockMvc.perform(get("/api/categories")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    /**
     * Prueba del camino feliz: Obtención exitosa de categoría por ID.
     *
     * Tipo de prueba: Funcional positivo (Camino feliz)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void obtenerCategoriaPorId_WithValidId_ReturnsCategory() throws Exception {
        // ==================== ARRANGE ====================
        CategoriaDTO categoria = new CategoriaDTO();
        categoria.setId(CATEGORY_ID);
        categoria.setNombre("Alimentación");

        given(categoriaService.obtenerCategoriaPorId(CATEGORY_ID)).willReturn(categoria);

        // ==================== ACT & ASSERT ====================
        mockMvc.perform(get("/api/categories/{categoryId}", CATEGORY_ID)
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(CATEGORY_ID))
                .andExpect(jsonPath("$.nombre").value("Alimentación"));
    }

    /**
     * Prueba de excepción: Obtención de categoría inexistente.
     *
     * Tipo de prueba: Validación negativa (Recurso no encontrado)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void obtenerCategoriaPorId_WithNonexistentId_ReturnsNotFound() throws Exception {
        // ==================== ARRANGE ====================
        Long nonexistentId = 999L;

        given(categoriaService.obtenerCategoriaPorId(nonexistentId))
                .willThrow(new RuntimeException("Categoría no encontrada"));

        // ==================== ACT & ASSERT ====================
        mockMvc.perform(get("/api/categories/{categoryId}", nonexistentId)
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isNotFound());
    }

    /**
     * Prueba del camino feliz: Eliminación exitosa de categoría.
     *
     * Tipo de prueba: Funcional positivo (Camino feliz)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void eliminarCategoria_WithValidId_ReturnsOk() throws Exception {
        // ==================== ARRANGE ====================
        doNothing().when(categoriaService).eliminarCategoria(CATEGORY_ID);

        // ==================== ACT & ASSERT ====================
        mockMvc.perform(delete("/api/categories/{categoryId}", CATEGORY_ID)
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk());
    }

    /**
     * Prueba de excepción: Eliminación de categoría inexistente.
     *
     * Tipo de prueba: Validación negativa (Recurso no encontrado)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void eliminarCategoria_WithNonexistentId_ReturnsNotFound() throws Exception {
        // ==================== ARRANGE ====================
        Long nonexistentId = 999L;

        org.mockito.Mockito.doThrow(new RuntimeException("Categoría no encontrada"))
                .when(categoriaService).eliminarCategoria(nonexistentId);

        // ==================== ACT & ASSERT ====================
        mockMvc.perform(delete("/api/categories/{categoryId}", nonexistentId)
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isNotFound());
    }
}

