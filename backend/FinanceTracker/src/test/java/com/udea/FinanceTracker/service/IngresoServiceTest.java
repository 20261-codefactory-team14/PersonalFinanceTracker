package com.udea.FinanceTracker.service;

import com.udea.FinanceTracker.dto.IngresoDTO;
import com.udea.FinanceTracker.dto.IngresoResponseDTO;
import com.udea.FinanceTracker.entity.Ingreso;
import com.udea.FinanceTracker.entity.Usuario;
import com.udea.FinanceTracker.repository.IngresoRepository;
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

@ExtendWith(MockitoExtension.class)
class IngresoServiceTest {

    @Mock
    private IngresoRepository ingresoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private IngresoService ingresoService;

    private static final Long USER_ID = 1L;
    private static final Long CATEGORY_ID = 2L;

    private static final BigDecimal MONTO = new BigDecimal("2000.00");
    private static final BigDecimal MONTO_DECIMAL = new BigDecimal("0.45");

    @Test
    void crearIngreso_WithDecimalMonto_PreservesExactPrecision() {

        // ==================== ARRANGE ====================

        IngresoDTO dto = new IngresoDTO();
        dto.setValor(MONTO_DECIMAL);
        dto.setFecha(LocalDate.now());
        dto.setDescripcion("Intereses bancarios");
        dto.setIdUsuario(USER_ID);
        dto.setIdCategoria(CATEGORY_ID);

        Usuario usuario = new Usuario();
        usuario.setId(USER_ID);
        usuario.setEmail("test@example.com");

        Ingreso ingresoGuardado = new Ingreso(
                MONTO_DECIMAL,
                LocalDate.now(),
                "Intereses bancarios",
                usuario,
                CATEGORY_ID
        );

        ingresoGuardado.setId(1L);

        given(usuarioRepository.findById(USER_ID))
                .willReturn(Optional.of(usuario));

        given(ingresoRepository.save(any(Ingreso.class)))
                .willReturn(ingresoGuardado);

        // ==================== ACT ====================

        IngresoResponseDTO response = ingresoService.crearIngreso(dto);

        // ==================== ASSERT ====================

        assertThat(response).isNotNull();

        assertThat(response.getValor())
                .as("El valor decimal debe preservarse exactamente")
                .isEqualByComparingTo(MONTO_DECIMAL);

        assertThat(response.getValor().toString())
                .as("La representación String debe ser exactamente '0.45'")
                .isEqualTo("0.45");

        assertThat(response.getIdCategoria())
                .isEqualTo(CATEGORY_ID);

        verify(usuarioRepository).findById(USER_ID);
        verify(ingresoRepository).save(any(Ingreso.class));
    }

    @Test
    void crearIngreso_WithNonExistentUser_ThrowsException() {

        // ==================== ARRANGE ====================

        IngresoDTO dto = new IngresoDTO();
        dto.setValor(MONTO);
        dto.setFecha(LocalDate.now());
        dto.setDescripcion("Salario");
        dto.setIdUsuario(999L);
        dto.setIdCategoria(CATEGORY_ID);

        given(usuarioRepository.findById(999L))
                .willReturn(Optional.empty());

        // ==================== ACT & ASSERT ====================

        assertThatThrownBy(() -> ingresoService.crearIngreso(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuario no encontrado");
    }

    @Test
    void crearIngreso_WithValidData_ReturnsIngresoResponse() {

        // ==================== ARRANGE ====================

        IngresoDTO dto = new IngresoDTO();
        dto.setValor(MONTO);
        dto.setFecha(LocalDate.now());
        dto.setDescripcion("Salario Mensual");
        dto.setIdUsuario(USER_ID);
        dto.setIdCategoria(CATEGORY_ID);

        Usuario usuario = new Usuario();
        usuario.setId(USER_ID);

        Ingreso ingresoGuardado = new Ingreso(
                MONTO,
                LocalDate.now(),
                "Salario Mensual",
                usuario,
                CATEGORY_ID
        );

        ingresoGuardado.setId(1L);

        given(usuarioRepository.findById(USER_ID))
                .willReturn(Optional.of(usuario));

        given(ingresoRepository.save(any(Ingreso.class)))
                .willReturn(ingresoGuardado);

        // ==================== ACT ====================

        IngresoResponseDTO response = ingresoService.crearIngreso(dto);

        // ==================== ASSERT ====================

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getValor()).isEqualByComparingTo(MONTO);
        assertThat(response.getDescripcion()).isEqualTo("Salario Mensual");
        assertThat(response.getIdUsuario()).isEqualTo(USER_ID);
        assertThat(response.getIdCategoria()).isEqualTo(CATEGORY_ID);
    }
}