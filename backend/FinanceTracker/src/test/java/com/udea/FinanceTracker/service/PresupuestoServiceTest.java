package com.udea.FinanceTracker.service;

import com.udea.FinanceTracker.dto.CrearPresupuestoRequest;
import com.udea.FinanceTracker.dto.PresupuestoDTO;
import com.udea.FinanceTracker.entity.Presupuesto;
import com.udea.FinanceTracker.mapper.PresupuestoMapper;
import com.udea.FinanceTracker.repository.PresupuestoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.never;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PresupuestoServiceTest {

    @Mock
    private PresupuestoRepository presupuestoRepository;

    @Mock
    private PresupuestoMapper presupuestoMapper;

    @InjectMocks
    private PresupuestoService presupuestoService;

    private static final Long USER_ID = 1L;
    private static final BigDecimal MONTO_VALIDO = new BigDecimal("1500.00");
    private static final BigDecimal MONTO_CERO = BigDecimal.ZERO;
    private static final BigDecimal MONTO_NEGATIVO = new BigDecimal("-500.00");

    /**
     * Prueba del camino feliz: Establecer presupuesto global válido.
     *
     * Tipo de prueba: Funcional positivo (Camino Feliz)
     * Patrón AAA: Arrange, Act, Assert
     *
     * Corresponde al CP-007-A: Establecer presupuesto global válido
     */
    @Test
    void crearPresupuesto_WithValidMonto_Success() throws Exception {
        // ==================== ARRANGE ====================
        CrearPresupuestoRequest request = new CrearPresupuestoRequest();
        request.setValor(MONTO_VALIDO);

        // Simular que no existe presupuesto activo
        given(presupuestoRepository.existsPresupuestoActivoByUsuario(any(Long.class), any(LocalDate.class)))
                .willReturn(false);

        Presupuesto presupuestoGuardado = new Presupuesto(MONTO_VALIDO, LocalDate.now(), USER_ID);
        presupuestoGuardado.setId(1L);

        PresupuestoDTO expectedDto = new PresupuestoDTO();
        expectedDto.setId(1L);
        expectedDto.setValor(MONTO_VALIDO);

        given(presupuestoRepository.save(any(Presupuesto.class))).willReturn(presupuestoGuardado);
        given(presupuestoMapper.toDTO(presupuestoGuardado)).willReturn(expectedDto);

        // ==================== ACT ====================
        PresupuestoDTO result = presupuestoService.crearPresupuesto(USER_ID, request);

        // ==================== ASSERT ====================
        assertThat(result).isNotNull();
        assertThat(result.getValor()).isEqualByComparingTo(MONTO_VALIDO);
        verify(presupuestoRepository).save(any(Presupuesto.class));
    }

    /**
     * Prueba de excepción: Validación de monto positivo (monto = 0).
     *
     * Tipo de prueba: Validación negativa (Excepción)
     * Patrón AAA: Arrange, Act, Assert
     *
     * Corresponde al CP-007-B: Validación de monto positivo en presupuesto
     */
    @Test
    void crearPresupuesto_WithZeroMonto_ThrowsException() {
        // ==================== ARRANGE ====================
        CrearPresupuestoRequest request = new CrearPresupuestoRequest();
        request.setValor(MONTO_CERO);  // Monto 0 (inválido)

        // ==================== ACT & ASSERT ====================
        assertThatThrownBy(() -> presupuestoService.crearPresupuesto(USER_ID, request))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("El valor del presupuesto debe ser mayor a cero");

        // Verificar que NO se intentó guardar ni buscar presupuesto activo
        verify(presupuestoRepository, never()).existsPresupuestoActivoByUsuario(any(Long.class), any(LocalDate.class));
        verify(presupuestoRepository, never()).save(any(Presupuesto.class));
    }

    /**
     * Prueba de excepción: Validación de monto positivo (monto negativo).
     *
     * Tipo de prueba: Validación negativa (Excepción)
     * Patrón AAA: Arrange, Act, Assert
     *
     * Corresponde al CP-007-B: Validación de monto positivo en presupuesto
     */
    @Test
    void crearPresupuesto_WithNegativeMonto_ThrowsException() {
        // ==================== ARRANGE ====================
        CrearPresupuestoRequest request = new CrearPresupuestoRequest();
        request.setValor(MONTO_NEGATIVO);  // Monto negativo (inválido)

        // ==================== ACT & ASSERT ====================
        assertThatThrownBy(() -> presupuestoService.crearPresupuesto(USER_ID, request))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("El valor del presupuesto debe ser mayor a cero");

        // Verificar que NO se intentó guardar ni buscar presupuesto activo
        verify(presupuestoRepository, never()).existsPresupuestoActivoByUsuario(any(Long.class), any(LocalDate.class));
        verify(presupuestoRepository, never()).save(any(Presupuesto.class));
    }

    /**
     * Prueba de excepción: Validación de monto nulo.
     *
     * Tipo de prueba: Validación negativa (Excepción)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void crearPresupuesto_WithNullMonto_ThrowsException() {
        // ==================== ARRANGE ====================
        CrearPresupuestoRequest request = new CrearPresupuestoRequest();
        request.setValor(null);  // Monto nulo (inválido)

        // ==================== ACT & ASSERT ====================
        assertThatThrownBy(() -> presupuestoService.crearPresupuesto(USER_ID, request))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("El valor del presupuesto debe ser mayor a cero");

        verify(presupuestoRepository, never()).existsPresupuestoActivoByUsuario(any(Long.class), any(LocalDate.class));
        verify(presupuestoRepository, never()).save(any(Presupuesto.class));
    }

    /**
     * Prueba de excepción: Usuario intenta crear presupuesto cuando ya tiene uno activo.
     *
     * Tipo de prueba: Validación negativa (Excepción)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void crearPresupuesto_WithExistingActivePresupuesto_ThrowsException() throws Exception {
        // ==================== ARRANGE ====================
        CrearPresupuestoRequest request = new CrearPresupuestoRequest();
        request.setValor(MONTO_VALIDO);

        // Simular que ya existe presupuesto activo
        given(presupuestoRepository.existsPresupuestoActivoByUsuario(any(Long.class), any(LocalDate.class)))
                .willReturn(true);

        // ==================== ACT & ASSERT ====================
        assertThatThrownBy(() -> presupuestoService.crearPresupuesto(USER_ID, request))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("El usuario ya tiene un presupuesto activo este mes");

        verify(presupuestoRepository, never()).save(any(Presupuesto.class));
    }

    /**
     * Prueba del camino feliz: Obtención exitosa de presupuesto activo.
     *
     * Tipo de prueba: Funcional positivo (Camino Feliz)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void obtenerPresupuestoActivo_WithExistingBudget_ReturnsBudget() throws Exception {
        // ==================== ARRANGE ====================
        Presupuesto presupuestoActivo = new Presupuesto(MONTO_VALIDO, LocalDate.now(), USER_ID);
        presupuestoActivo.setId(1L);

        PresupuestoDTO expectedDto = new PresupuestoDTO();
        expectedDto.setId(1L);
        expectedDto.setValor(MONTO_VALIDO);

        given(presupuestoRepository.findPresupuestoActivoByUsuario(any(Long.class), any(LocalDate.class)))
                .willReturn(Optional.of(presupuestoActivo));
        given(presupuestoMapper.toDTO(presupuestoActivo)).willReturn(expectedDto);

        // ==================== ACT ====================
        PresupuestoDTO result = presupuestoService.obtenerPresupuestoActivo(USER_ID);

        // ==================== ASSERT ====================
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getValor()).isEqualByComparingTo(MONTO_VALIDO);

        verify(presupuestoRepository).findPresupuestoActivoByUsuario(any(Long.class), any(LocalDate.class));
        verify(presupuestoMapper).toDTO(presupuestoActivo);
    }

    /**
     * Prueba de excepción: Usuario intenta obtener presupuesto activo cuando no tiene.
     *
     * Tipo de prueba: Validación negativa (Excepción)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void obtenerPresupuestoActivo_WithNoActiveBudget_ThrowsException() {
        // ==================== ARRANGE ====================
        given(presupuestoRepository.findPresupuestoActivoByUsuario(any(Long.class), any(LocalDate.class)))
                .willReturn(Optional.empty());

        // ==================== ACT & ASSERT ====================
        assertThatThrownBy(() -> presupuestoService.obtenerPresupuestoActivo(USER_ID))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("El usuario no tiene un presupuesto activo");
    }

    /**
     * Prueba del camino feliz: Obtención exitosa de todos los presupuestos.
     *
     * Tipo de prueba: Funcional positivo (Camino Feliz)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void obtenerTodosLosPresupuestos_WithMultipleBudgets_ReturnsList() throws Exception {
        // ==================== ARRANGE ====================
        Presupuesto presupuesto1 = new Presupuesto(MONTO_VALIDO, LocalDate.now(), USER_ID);
        presupuesto1.setId(1L);

        Presupuesto presupuesto2 = new Presupuesto(MONTO_VALIDO, LocalDate.now().minusMonths(1), USER_ID);
        presupuesto2.setId(2L);

        List<Presupuesto> presupuestos = Arrays.asList(presupuesto1, presupuesto2);

        PresupuestoDTO dto1 = new PresupuestoDTO();
        dto1.setId(1L);
        dto1.setValor(MONTO_VALIDO);

        PresupuestoDTO dto2 = new PresupuestoDTO();
        dto2.setId(2L);
        dto2.setValor(MONTO_VALIDO);

        given(presupuestoRepository.findByIdUsuarioOrderByFechaDesc(USER_ID))
                .willReturn(presupuestos);
        given(presupuestoMapper.toDTO(presupuesto1)).willReturn(dto1);
        given(presupuestoMapper.toDTO(presupuesto2)).willReturn(dto2);

        // ==================== ACT ====================
        List<PresupuestoDTO> result = presupuestoService.obtenerTodosLosPresupuestos(USER_ID);

        // ==================== ASSERT ====================
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(2L);

        verify(presupuestoRepository).findByIdUsuarioOrderByFechaDesc(USER_ID);
    }

    /**
     * Prueba de excepción: Usuario intenta obtener presupuestos cuando no tiene ninguno.
     *
     * Tipo de prueba: Validación negativa (Excepción)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void obtenerTodosLosPresupuestos_WithNoBudgets_ThrowsException() {
        // ==================== ARRANGE ====================
        given(presupuestoRepository.findByIdUsuarioOrderByFechaDesc(USER_ID))
                .willReturn(new ArrayList<>());

        // ==================== ACT & ASSERT ====================
        assertThatThrownBy(() -> presupuestoService.obtenerTodosLosPresupuestos(USER_ID))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("El usuario no tiene presupuestos registrados");
    }

    /**
     * Prueba de precisión: Soporte de decimales en presupuestos.
     *
     * Tipo de prueba: Funcional positivo (Precisión)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void crearPresupuesto_WithDecimalMonto_PreservesExactPrecision() throws Exception {
        // ==================== ARRANGE ====================
        BigDecimal montoDecimal = new BigDecimal("1500.99");

        CrearPresupuestoRequest request = new CrearPresupuestoRequest();
        request.setValor(montoDecimal);

        given(presupuestoRepository.existsPresupuestoActivoByUsuario(any(Long.class), any(LocalDate.class)))
                .willReturn(false);

        Presupuesto presupuestoGuardado = new Presupuesto(montoDecimal, LocalDate.now(), USER_ID);
        presupuestoGuardado.setId(1L);

        PresupuestoDTO expectedDto = new PresupuestoDTO();
        expectedDto.setId(1L);
        expectedDto.setValor(montoDecimal);

        given(presupuestoRepository.save(any(Presupuesto.class))).willReturn(presupuestoGuardado);
        given(presupuestoMapper.toDTO(presupuestoGuardado)).willReturn(expectedDto);

        // ==================== ACT ====================
        PresupuestoDTO result = presupuestoService.crearPresupuesto(USER_ID, request);

        // ==================== ASSERT ====================
        assertThat(result.getValor()).isEqualByComparingTo(montoDecimal);
        assertThat(result.getValor().toString()).isEqualTo("1500.99");
    }
}