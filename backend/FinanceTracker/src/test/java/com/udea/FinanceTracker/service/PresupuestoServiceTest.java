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
        request.setValor(MONTO_CERO);

        given(presupuestoRepository.existsPresupuestoActivoByUsuario(any(Long.class), any(LocalDate.class)))
                .willReturn(false);

        // ==================== ACT & ASSERT ====================
        // La validación @DecimalMin(value = "0.0", inclusive = false) debe activarse
        assertThatThrownBy(() -> presupuestoService.crearPresupuesto(USER_ID, request))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("mayor a cero");

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
        request.setValor(MONTO_NEGATIVO);

        given(presupuestoRepository.existsPresupuestoActivoByUsuario(any(Long.class), any(LocalDate.class)))
                .willReturn(false);

        // ==================== ACT & ASSERT ====================
        assertThatThrownBy(() -> presupuestoService.crearPresupuesto(USER_ID, request))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("mayor a cero");

        verify(presupuestoRepository, never()).save(any(Presupuesto.class));
    }
}