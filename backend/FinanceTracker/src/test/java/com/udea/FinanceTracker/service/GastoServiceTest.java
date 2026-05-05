package com.udea.FinanceTracker.service;

import com.udea.FinanceTracker.dto.GastoDTO;
import com.udea.FinanceTracker.dto.GastoResponseDTO;
import com.udea.FinanceTracker.entity.Categoria;
import com.udea.FinanceTracker.entity.Gasto;
import com.udea.FinanceTracker.entity.Usuario;
import com.udea.FinanceTracker.repository.CategoriaRepository;
import com.udea.FinanceTracker.repository.GastoRepository;
import com.udea.FinanceTracker.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class GastoServiceTest {

    @Mock
    private GastoRepository gastoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private GastoService gastoService;

    private static final Long USER_ID = 1L;
    private static final Long CATEGORIA_ID = 1L;
    private static final BigDecimal MONTO = new BigDecimal("25.50");

    /**
     * Prueba del camino feliz: Creación exitosa de gasto.
     *
     * Tipo de prueba: Funcional positivo (Camino Feliz)
     * Patrón AAA: Arrange, Act, Assert
     *
     * Corresponde al CP-005-A: Creación exitosa de gasto
     */
    @Test
    void crearGasto_WithValidData_ReturnsGastoResponse() {
        // ==================== ARRANGE ====================
        // Configurar DTO de entrada
        GastoDTO dto = new GastoDTO();
        dto.setValor(MONTO);
        dto.setFecha(LocalDate.now());
        dto.setDescripcion("Almuerzo");
        dto.setIdUsuario(USER_ID);
        dto.setIdCategoria(CATEGORIA_ID);

        // Configurar entities simuladas
        Usuario usuario = new Usuario();
        usuario.setId(USER_ID);

        Categoria categoria = new Categoria();
        categoria.setId(CATEGORIA_ID);
        categoria.setNombre("Alimentación");

        Gasto gastoGuardado = new Gasto(MONTO, LocalDate.now(), "Almuerzo", usuario, categoria);
        gastoGuardado.setId(1L);

        // Configurar mocks
        given(usuarioRepository.findById(USER_ID)).willReturn(Optional.of(usuario));
        given(categoriaRepository.findById(CATEGORIA_ID)).willReturn(Optional.of(categoria));
        given(gastoRepository.save(any(Gasto.class))).willReturn(gastoGuardado);

        // ==================== ACT ====================
        GastoResponseDTO response = gastoService.crearGasto(dto);

        // ==================== ASSERT ====================
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getValor()).isEqualByComparingTo(MONTO);
        assertThat(response.getDescripcion()).isEqualTo("Almuerzo");
        assertThat(response.getIdUsuario()).isEqualTo(USER_ID);
        assertThat(response.getIdCategoria()).isEqualTo(CATEGORIA_ID);

        verify(usuarioRepository).findById(USER_ID);
        verify(categoriaRepository).findById(CATEGORIA_ID);
        verify(gastoRepository).save(any(Gasto.class));
    }

    /**
     * Prueba de excepción: Creación de gasto con usuario inexistente.
     *
     * Tipo de prueba: Validación negativa (Excepción)
     * Patrón AAA: Arrange, Act, Assert
     *
     * Corresponde al CP-005-A (variante de excepción)
     */
    @Test
    void crearGasto_WithNonExistentUser_ThrowsException() {
        // ==================== ARRANGE ====================
        GastoDTO dto = new GastoDTO();
        dto.setValor(MONTO);
        dto.setFecha(LocalDate.now());
        dto.setIdUsuario(999L);
        dto.setIdCategoria(CATEGORIA_ID);

        given(usuarioRepository.findById(999L)).willReturn(Optional.empty());

        // ==================== ACT & ASSERT ====================
        assertThatThrownBy(() -> gastoService.crearGasto(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuario no encontrado");
    }

    /**
     * Prueba de excepción: Creación de gasto con categoría inexistente.
     *
     * Tipo de prueba: Validación negativa (Excepción)
     * Patrón AAA: Arrange, Act, Assert
     *
     * Corresponde al CP-006-A (variante de excepción)
     */
    @Test
    void crearGasto_WithNonExistentCategoria_ThrowsException() {
        // ==================== ARRANGE ====================
        GastoDTO dto = new GastoDTO();
        dto.setValor(MONTO);
        dto.setFecha(LocalDate.now());
        dto.setIdUsuario(USER_ID);
        dto.setIdCategoria(999L);

        Usuario usuario = new Usuario();
        usuario.setId(USER_ID);

        given(usuarioRepository.findById(USER_ID)).willReturn(Optional.of(usuario));
        given(categoriaRepository.findById(999L)).willReturn(Optional.empty());

        // ==================== ACT & ASSERT ====================
        assertThatThrownBy(() -> gastoService.crearGasto(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Categoría no encontrada");
    }

    /**
     * Prueba de excepción: Actualización de gasto cambiando categoría.
     *
     * Tipo de prueba: Funcional positivo (Camino Feliz)
     * Patrón AAA: Arrange, Act, Assert
     *
     * Corresponde al CP-006-B: Cambio de categoría (Recategorización)
     */
    @Test
    void actualizarGasto_ChangeCategoria_Success() {
        // ==================== ARRANGE ====================
        Long gastoId = 1L;
        Long newCategoriaId = 3L;

        // Configurar gasto existente
        Usuario usuario = new Usuario();
        usuario.setId(USER_ID);

        Categoria categoriaOriginal = new Categoria();
        categoriaOriginal.setId(2L); // "Otros"

        Gasto gastoExistente = new Gasto(MONTO, LocalDate.now(), "Almuerzo", usuario, categoriaOriginal);
        gastoExistente.setId(gastoId);

        // Configurar nueva categoría
        Categoria nuevaCategoria = new Categoria();
        nuevaCategoria.setId(newCategoriaId);
        nuevaCategoria.setNombre("Salud");

        // Configurar DTO con nueva categoría
        GastoDTO dto = new GastoDTO();
        dto.setValor(MONTO);
        dto.setFecha(LocalDate.now());
        dto.setDescripcion("Almuerzo");
        dto.setIdUsuario(USER_ID);
        dto.setIdCategoria(newCategoriaId);

        given(gastoRepository.findById(gastoId)).willReturn(Optional.of(gastoExistente));
        given(usuarioRepository.findById(USER_ID)).willReturn(Optional.of(usuario));
        given(categoriaRepository.findById(newCategoriaId)).willReturn(Optional.of(nuevaCategoria));
        given(gastoRepository.save(any(Gasto.class))).willAnswer(invocation -> invocation.getArgument(0));

        // ==================== ACT ====================
        GastoResponseDTO response = gastoService.actualizarGasto(gastoId, dto);

        // ==================== ASSERT ====================
        assertThat(response.getIdCategoria()).isEqualTo(newCategoriaId);
        verify(gastoRepository).save(any(Gasto.class));
    }

    /**
     * Prueba de excepción: Creación de gasto con monto nulo.
     *
     * Tipo de prueba: Validación negativa (Excepción)
     * Patrón AAA: Arrange, Act, Assert
     *
     * Corresponde al CP-005-B: Validación de campos obligatorios en gasto
     */
    @Test
    void crearGasto_WithNullMonto_ThrowsException() {
        // ==================== ARRANGE ====================
        GastoDTO dto = new GastoDTO();
        dto.setValor(null);  // ✅ Monto nulo (campo obligatorio)
        dto.setFecha(LocalDate.now());
        dto.setDescripcion("Almuerzo");
        dto.setIdUsuario(USER_ID);
        dto.setIdCategoria(CATEGORIA_ID);

        // ==================== ACT & ASSERT ====================
        assertThatThrownBy(() -> gastoService.crearGasto(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("El valor del gasto es obligatorio");

        // Verificar que NO se intentó guardar el gasto
        verify(gastoRepository, never()).save(any(Gasto.class));

        // Verificar que nunca se buscó el usuario ni la categoría
        verify(usuarioRepository, never()).findById(anyLong());
        verify(categoriaRepository, never()).findById(anyLong());
    }
}