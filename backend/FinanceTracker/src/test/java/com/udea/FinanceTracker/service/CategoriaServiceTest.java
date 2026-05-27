package com.udea.FinanceTracker.service;

import com.udea.FinanceTracker.dto.ActualizarCategoriaRequest;
import com.udea.FinanceTracker.dto.CategoriaDTO;
import com.udea.FinanceTracker.dto.CrearCategoriaRequest;
import com.udea.FinanceTracker.entity.Categoria;
import com.udea.FinanceTracker.mapper.CategoriaMapper;
import com.udea.FinanceTracker.repository.CategoriaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

/**
 * Pruebas unitarias para CategoriaService.
 * Valida las operaciones CRUD de categorías de gastos.
 *
 * Patrón Triple AAA aplicado en cada prueba.
 *
 */
@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private CategoriaMapper categoriaMapper;

    @InjectMocks
    private CategoriaService categoriaService;

    private static final String CATEGORIA_NOMBRE = "Alimentación";
    private static final String CATEGORIA_NORMALIZADA = "Alimentación";

    // ==================== HAPPY PATH TESTS ====================

    /**
     * Prueba del camino feliz: Creación exitosa de categoría.
     *
     * Tipo de prueba: Funcional positivo (Camino Feliz)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void crearCategoria_WithValidName_ReturnsCategoriaDTOSuccess() throws Exception {
        // ==================== ARRANGE ====================
        CrearCategoriaRequest request = new CrearCategoriaRequest();
        request.setNombre(CATEGORIA_NOMBRE);

        Categoria categoriaGuardada = new Categoria(CATEGORIA_NORMALIZADA);
        categoriaGuardada.setId(1L);

        CategoriaDTO expectedDto = new CategoriaDTO();
        expectedDto.setId(1L);
        expectedDto.setNombre(CATEGORIA_NORMALIZADA);

        given(categoriaRepository.existsByNombreIgnoreCase(CATEGORIA_NORMALIZADA))
                .willReturn(false);
        given(categoriaRepository.save(any(Categoria.class)))
                .willReturn(categoriaGuardada);
        given(categoriaMapper.toDTO(categoriaGuardada))
                .willReturn(expectedDto);

        // ==================== ACT ====================
        CategoriaDTO result = categoriaService.crearCategoria(request);

        // ==================== ASSERT ====================
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNombre()).isEqualTo(CATEGORIA_NORMALIZADA);

        verify(categoriaRepository).existsByNombreIgnoreCase(CATEGORIA_NORMALIZADA);
        verify(categoriaRepository).save(any(Categoria.class));
        verify(categoriaMapper).toDTO(categoriaGuardada);
    }

    /**
     * Prueba de excepción: Creación de categoría con nombre duplicado.
     *
     * Tipo de prueba: Validación negativa (Excepción)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void crearCategoria_WithDuplicateName_ThrowsException() {
        // ==================== ARRANGE ====================
        CrearCategoriaRequest request = new CrearCategoriaRequest();
        request.setNombre(CATEGORIA_NOMBRE);

        given(categoriaRepository.existsByNombreIgnoreCase(CATEGORIA_NORMALIZADA))
                .willReturn(true);

        // ==================== ACT & ASSERT ====================
        assertThatThrownBy(() -> categoriaService.crearCategoria(request))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("Ya existe una categoría con ese nombre");

        verify(categoriaRepository, never()).save(any(Categoria.class));
    }

    /**
     * Prueba del camino feliz: Normalización de nombre (espacios).
     *
     * Tipo de prueba: Funcional positivo (Normalización)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void crearCategoria_WithSpacesInName_NormalizesAndCreates() throws Exception {
        // ==================== ARRANGE ====================
        String nombreConEspacios = "  Salud y Bienestar  ";
        String nombreNormalizado = "Salud y Bienestar";

        CrearCategoriaRequest request = new CrearCategoriaRequest();
        request.setNombre(nombreConEspacios);

        Categoria categoriaGuardada = new Categoria(nombreNormalizado);
        categoriaGuardada.setId(1L);

        CategoriaDTO expectedDto = new CategoriaDTO();
        expectedDto.setId(1L);
        expectedDto.setNombre(nombreNormalizado);

        given(categoriaRepository.existsByNombreIgnoreCase(nombreNormalizado))
                .willReturn(false);
        given(categoriaRepository.save(any(Categoria.class)))
                .willReturn(categoriaGuardada);
        given(categoriaMapper.toDTO(categoriaGuardada))
                .willReturn(expectedDto);

        // ==================== ACT ====================
        CategoriaDTO result = categoriaService.crearCategoria(request);

        // ==================== ASSERT ====================
        assertThat(result.getNombre()).isEqualTo(nombreNormalizado);
    }

    /**
     * Prueba del camino feliz: Obtención exitosa de categoría por ID.
     *
     * Tipo de prueba: Funcional positivo (Camino Feliz)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void obtenerCategoriaPorId_WithValidId_ReturnsCategoriaDTO() throws Exception {
        // ==================== ARRANGE ====================
        Long categoriaId = 1L;

        Categoria categoria = new Categoria(CATEGORIA_NOMBRE);
        categoria.setId(categoriaId);

        CategoriaDTO expectedDto = new CategoriaDTO();
        expectedDto.setId(categoriaId);
        expectedDto.setNombre(CATEGORIA_NOMBRE);

        given(categoriaRepository.findById(categoriaId))
                .willReturn(Optional.of(categoria));
        given(categoriaMapper.toDTO(categoria))
                .willReturn(expectedDto);

        // ==================== ACT ====================
        CategoriaDTO result = categoriaService.obtenerCategoriaPorId(categoriaId);

        // ==================== ASSERT ====================
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(categoriaId);
        assertThat(result.getNombre()).isEqualTo(CATEGORIA_NOMBRE);

        verify(categoriaRepository).findById(categoriaId);
        verify(categoriaMapper).toDTO(categoria);
    }

    /**
     * Prueba de excepción: Obtención de categoría con ID inexistente.
     *
     * Tipo de prueba: Validación negativa (Excepción)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void obtenerCategoriaPorId_WithNonExistentId_ThrowsException() {
        // ==================== ARRANGE ====================
        Long categoriaId = 999L;

        given(categoriaRepository.findById(categoriaId))
                .willReturn(Optional.empty());

        // ==================== ACT & ASSERT ====================
        assertThatThrownBy(() -> categoriaService.obtenerCategoriaPorId(categoriaId))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("Categoría no encontrada");
    }

    /**
     * Prueba del camino feliz: Actualización exitosa de categoría.
     *
     * Tipo de prueba: Funcional positivo (Camino Feliz)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void actualizarCategoria_WithValidData_Success() throws Exception {
        // ==================== ARRANGE ====================
        Long categoriaId = 1L;
        String nuevoNombre = "Entretenimiento";

        Categoria categoriaExistente = new Categoria(CATEGORIA_NOMBRE);
        categoriaExistente.setId(categoriaId);

        ActualizarCategoriaRequest request = new ActualizarCategoriaRequest();
        request.setNombre(nuevoNombre);

        Categoria categoriaActualizada = new Categoria(nuevoNombre);
        categoriaActualizada.setId(categoriaId);

        CategoriaDTO expectedDto = new CategoriaDTO();
        expectedDto.setId(categoriaId);
        expectedDto.setNombre(nuevoNombre);

        given(categoriaRepository.findById(categoriaId))
                .willReturn(Optional.of(categoriaExistente));
        given(categoriaRepository.existsByNombreIgnoreCaseAndIdNot(nuevoNombre, categoriaId))
                .willReturn(false);
        given(categoriaRepository.save(any(Categoria.class)))
                .willReturn(categoriaActualizada);
        given(categoriaMapper.toDTO(categoriaActualizada))
                .willReturn(expectedDto);

        // ==================== ACT ====================
        CategoriaDTO result = categoriaService.actualizarCategoria(categoriaId, request);

        // ==================== ASSERT ====================
        assertThat(result).isNotNull();
        assertThat(result.getNombre()).isEqualTo(nuevoNombre);

        verify(categoriaRepository).findById(categoriaId);
        verify(categoriaRepository).save(any(Categoria.class));
    }

    /**
     * Prueba de excepción: Actualización de categoría con ID inexistente.
     *
     * Tipo de prueba: Validación negativa (Excepción)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void actualizarCategoria_WithNonExistentId_ThrowsException() {
        // ==================== ARRANGE ====================
        Long categoriaId = 999L;

        ActualizarCategoriaRequest request = new ActualizarCategoriaRequest();
        request.setNombre("Nuevo Nombre");

        given(categoriaRepository.findById(categoriaId))
                .willReturn(Optional.empty());

        // ==================== ACT & ASSERT ====================
        assertThatThrownBy(() -> categoriaService.actualizarCategoria(categoriaId, request))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("Categoría no encontrada");

        verify(categoriaRepository, never()).save(any(Categoria.class));
    }

    /**
     * Prueba de excepción: Actualización de categoría con nombre duplicado.
     *
     * Tipo de prueba: Validación negativa (Excepción)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void actualizarCategoria_WithDuplicateName_ThrowsException() throws Exception {
        // ==================== ARRANGE ====================
        Long categoriaId = 1L;
        String nuevoNombre = "Transporte";

        Categoria categoriaExistente = new Categoria(CATEGORIA_NOMBRE);
        categoriaExistente.setId(categoriaId);

        ActualizarCategoriaRequest request = new ActualizarCategoriaRequest();
        request.setNombre(nuevoNombre);

        given(categoriaRepository.findById(categoriaId))
                .willReturn(Optional.of(categoriaExistente));
        given(categoriaRepository.existsByNombreIgnoreCaseAndIdNot(nuevoNombre, categoriaId))
                .willReturn(true);

        // ==================== ACT & ASSERT ====================
        assertThatThrownBy(() -> categoriaService.actualizarCategoria(categoriaId, request))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("Ya existe otra categoría con ese nombre");

        verify(categoriaRepository, never()).save(any(Categoria.class));
    }

    /**
     * Prueba del camino feliz: Obtención de todas las categorías.
     *
     * Tipo de prueba: Funcional positivo (Camino Feliz)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void obtenerCategorias_WithMultipleCategories_ReturnsList() {
        // ==================== ARRANGE ====================
        Categoria cat1 = new Categoria("Alimentación");
        cat1.setId(1L);

        Categoria cat2 = new Categoria("Transporte");
        cat2.setId(2L);

        List<Categoria> categorias = Arrays.asList(cat1, cat2);

        CategoriaDTO dto1 = new CategoriaDTO();
        dto1.setId(1L);
        dto1.setNombre("Alimentación");

        CategoriaDTO dto2 = new CategoriaDTO();
        dto2.setId(2L);
        dto2.setNombre("Transporte");

        given(categoriaRepository.findAllByOrderByNombreAsc())
                .willReturn(categorias);
        given(categoriaMapper.toDTO(cat1)).willReturn(dto1);
        given(categoriaMapper.toDTO(cat2)).willReturn(dto2);

        // ==================== ACT ====================
        List<CategoriaDTO> result = categoriaService.obtenerCategorias();

        // ==================== ASSERT ====================
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getNombre()).isEqualTo("Alimentación");
        assertThat(result.get(1).getNombre()).isEqualTo("Transporte");

        verify(categoriaRepository).findAllByOrderByNombreAsc();
    }

    /**
     * Prueba del camino feliz: Obtención de categorías vacías.
     *
     * Tipo de prueba: Funcional positivo (Caso límite)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void obtenerCategorias_WithNoCategories_ReturnsEmptyList() {
        // ==================== ARRANGE ====================
        given(categoriaRepository.findAllByOrderByNombreAsc())
                .willReturn(new ArrayList<>());

        // ==================== ACT ====================
        List<CategoriaDTO> result = categoriaService.obtenerCategorias();

        // ==================== ASSERT ====================
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    /**
     * Prueba del camino feliz: Eliminación exitosa de categoría.
     *
     * Tipo de prueba: Funcional positivo (Camino Feliz)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void eliminarCategoria_WithValidId_Success() throws Exception {
        // ==================== ARRANGE ====================
        Long categoriaId = 1L;

        Categoria categoria = new Categoria(CATEGORIA_NOMBRE);
        categoria.setId(categoriaId);

        given(categoriaRepository.findById(categoriaId))
                .willReturn(Optional.of(categoria));

        // ==================== ACT ====================
        categoriaService.eliminarCategoria(categoriaId);

        // ==================== ASSERT ====================
        verify(categoriaRepository).findById(categoriaId);
        verify(categoriaRepository).delete(categoria);
    }

    /**
     * Prueba de excepción: Eliminación de categoría inexistente.
     *
     * Tipo de prueba: Validación negativa (Excepción)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void eliminarCategoria_WithNonExistentId_ThrowsException() {
        // ==================== ARRANGE ====================
        Long categoriaId = 999L;

        given(categoriaRepository.findById(categoriaId))
                .willReturn(Optional.empty());

        // ==================== ACT & ASSERT ====================
        assertThatThrownBy(() -> categoriaService.eliminarCategoria(categoriaId))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("Categoría no encontrada");

        verify(categoriaRepository, never()).delete(any(Categoria.class));
    }
}

