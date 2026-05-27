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

/**
 * Pruebas unitarias para IngresoService.
 * Valida las operaciones CRUD de ingresos financieros.
 *
 * Patrón Triple AAA aplicado en cada prueba.
 *
 * @author Equipo Quality Assurance
 */
@ExtendWith(MockitoExtension.class)
class IngresoServiceTest {

    @Mock
    private IngresoRepository ingresoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private IngresoService ingresoService;

    private static final Long USER_ID = 1L;
    private static final BigDecimal MONTO = new BigDecimal("2000.00");
    private static final BigDecimal MONTO_DECIMAL = new BigDecimal("0.45");

    /**
     * Prueba de precisión decimal: Soporte de decimales en ingresos.
     *
     * Tipo de prueba: Funcional positivo (Precisión)
     * Patrón AAA: Arrange, Act, Assert
     *
     * Corresponde al CP-004-B: Soporte de decimales en registro de ingresos
     * Verifica que el sistema maneja correctamente montos con decimales (centavos)
     * sin redondeos, usando BigDecimal para precisión exacta.
     */
    @Test
    void crearIngreso_WithDecimalMonto_PreservesExactPrecision() {
        // ==================== ARRANGE ====================
        // Configurar DTO de entrada con valor decimal exacto (0.45)
        IngresoDTO dto = new IngresoDTO();
        dto.setValor(MONTO_DECIMAL);
        dto.setFecha(LocalDate.now());
        dto.setDescripcion("Intereses bancarios");
        dto.setIdUsuario(USER_ID);

        // Configurar usuario existente
        Usuario usuario = new Usuario();
        usuario.setId(USER_ID);
        usuario.setEmail("test@example.com");

        // Configurar ingreso guardado con el mismo valor decimal
        Ingreso ingresoGuardado = new Ingreso(MONTO_DECIMAL, LocalDate.now(), "Intereses bancarios", usuario);
        ingresoGuardado.setId(1L);

        // Configurar mocks
        given(usuarioRepository.findById(USER_ID)).willReturn(Optional.of(usuario));
        given(ingresoRepository.save(any(Ingreso.class))).willReturn(ingresoGuardado);

        // ==================== ACT ====================
        IngresoResponseDTO response = ingresoService.crearIngreso(dto);

        // ==================== ASSERT ====================
        // Verificar que el valor decimal se conserva exactamente (sin redondeo)
        assertThat(response).isNotNull();
        assertThat(response.getValor())
                .as("El valor decimal debe preservarse exactamente, sin redondeos")
                .isEqualByComparingTo(MONTO_DECIMAL); // compareTo con BigDecimal

        // Verificar que no hay pérdida de precisión (0.45 no debe convertirse en 0.4499999999)
        assertThat(response.getValor().toString())
                .as("La representación String debe ser exactamente '0.45'")
                .isEqualTo("0.45");

        verify(usuarioRepository).findById(USER_ID);
        verify(ingresoRepository).save(any(Ingreso.class));
    }

    /**
     * Prueba de excepción: Usuario no encontrado al crear ingreso.
     *
     * Tipo de prueba: Validación negativa (Excepción)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void crearIngreso_WithNonExistentUser_ThrowsException() {
        // ==================== ARRANGE ====================
        IngresoDTO dto = new IngresoDTO();
        dto.setValor(MONTO);
        dto.setFecha(LocalDate.now());
        dto.setDescripcion("Salario");
        dto.setIdUsuario(999L);

        given(usuarioRepository.findById(999L)).willReturn(Optional.empty());

        // ==================== ACT & ASSERT ====================
        assertThatThrownBy(() -> ingresoService.crearIngreso(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuario no encontrado");
    }

    /**
     * Prueba del camino feliz: Registro exitoso de ingreso.
     *
     * Tipo de prueba: Funcional positivo (Camino Feliz)
     * Patrón AAA: Arrange, Act, Assert
     *
     * Corresponde al CP-004-A (base, sin categoría)
     */
    @Test
    void crearIngreso_WithValidData_ReturnsIngresoResponse() {
        // ==================== ARRANGE ====================
        IngresoDTO dto = new IngresoDTO();
        dto.setValor(MONTO);
        dto.setFecha(LocalDate.now());
        dto.setDescripcion("Salario Mensual");
        dto.setIdUsuario(USER_ID);

        Usuario usuario = new Usuario();
        usuario.setId(USER_ID);

        Ingreso ingresoGuardado = new Ingreso(MONTO, LocalDate.now(), "Salario Mensual", usuario);
        ingresoGuardado.setId(1L);

        given(usuarioRepository.findById(USER_ID)).willReturn(Optional.of(usuario));
        given(ingresoRepository.save(any(Ingreso.class))).willReturn(ingresoGuardado);

        // ==================== ACT ====================
        IngresoResponseDTO response = ingresoService.crearIngreso(dto);

        // ==================== ASSERT ====================
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getValor()).isEqualByComparingTo(MONTO);
        assertThat(response.getDescripcion()).isEqualTo("Salario Mensual");
        assertThat(response.getIdUsuario()).isEqualTo(USER_ID);
    }

    /**
     * Prueba del camino feliz: Obtención exitosa de ingreso por ID.
     *
     * Tipo de prueba: Funcional positivo (Camino Feliz)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void obtenerIngresoPorId_WithValidId_ReturnsIngresoResponse() {
        // ==================== ARRANGE ====================
        Long ingresoId = 1L;

        Usuario usuario = new Usuario();
        usuario.setId(USER_ID);

        Ingreso ingreso = new Ingreso(MONTO, LocalDate.now(), "Salario", usuario);
        ingreso.setId(ingresoId);

        given(ingresoRepository.findById(ingresoId)).willReturn(Optional.of(ingreso));

        // ==================== ACT ====================
        IngresoResponseDTO response = ingresoService.obtenerIngresoPorId(ingresoId);

        // ==================== ASSERT ====================
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(ingresoId);
        assertThat(response.getValor()).isEqualByComparingTo(MONTO);

        verify(ingresoRepository).findById(ingresoId);
    }

    /**
     * Prueba de excepción: Obtención de ingreso con ID inexistente.
     *
     * Tipo de prueba: Validación negativa (Excepción)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void obtenerIngresoPorId_WithNonExistentId_ThrowsException() {
        // ==================== ARRANGE ====================
        Long ingresoId = 999L;

        given(ingresoRepository.findById(ingresoId)).willReturn(Optional.empty());

        // ==================== ACT & ASSERT ====================
        assertThatThrownBy(() -> ingresoService.obtenerIngresoPorId(ingresoId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ingreso no encontrado");
    }

    /**
     * Prueba del camino feliz: Actualización exitosa de ingreso.
     *
     * Tipo de prueba: Funcional positivo (Camino Feliz)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void actualizarIngreso_WithValidData_Success() {
        // ==================== ARRANGE ====================
        Long ingresoId = 1L;
        BigDecimal newMonto = new BigDecimal("3000.00");

        Usuario usuario = new Usuario();
        usuario.setId(USER_ID);

        Ingreso ingresoExistente = new Ingreso(MONTO, LocalDate.now(), "Salario", usuario);
        ingresoExistente.setId(ingresoId);

        IngresoDTO dto = new IngresoDTO();
        dto.setValor(newMonto);
        dto.setFecha(LocalDate.now());
        dto.setDescripcion("Salario Aumentado");
        dto.setIdUsuario(USER_ID);

        given(ingresoRepository.findById(ingresoId)).willReturn(Optional.of(ingresoExistente));
        given(usuarioRepository.findById(USER_ID)).willReturn(Optional.of(usuario));
        given(ingresoRepository.save(any(Ingreso.class))).willAnswer(invocation -> invocation.getArgument(0));

        // ==================== ACT ====================
        IngresoResponseDTO response = ingresoService.actualizarIngreso(ingresoId, dto);

        // ==================== ASSERT ====================
        assertThat(response).isNotNull();
        assertThat(response.getValor()).isEqualByComparingTo(newMonto);
        assertThat(response.getDescripcion()).isEqualTo("Salario Aumentado");

        verify(ingresoRepository).findById(ingresoId);
        verify(ingresoRepository).save(any(Ingreso.class));
    }

    /**
     * Prueba de excepción: Actualización de ingreso inexistente.
     *
     * Tipo de prueba: Validación negativa (Excepción)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void actualizarIngreso_WithNonExistentIngreso_ThrowsException() {
        // ==================== ARRANGE ====================
        Long ingresoId = 999L;

        IngresoDTO dto = new IngresoDTO();
        dto.setValor(MONTO);
        dto.setFecha(LocalDate.now());
        dto.setIdUsuario(USER_ID);

        given(ingresoRepository.findById(ingresoId)).willReturn(Optional.empty());

        // ==================== ACT & ASSERT ====================
        assertThatThrownBy(() -> ingresoService.actualizarIngreso(ingresoId, dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ingreso no encontrado");
    }

    /**
     * Prueba de excepción: Actualización de ingreso con usuario inexistente.
     *
     * Tipo de prueba: Validación negativa (Excepción)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void actualizarIngreso_WithNonExistentUser_ThrowsException() {
        // ==================== ARRANGE ====================
        Long ingresoId = 1L;

        Usuario usuario = new Usuario();
        usuario.setId(USER_ID);

        Ingreso ingresoExistente = new Ingreso(MONTO, LocalDate.now(), "Salario", usuario);
        ingresoExistente.setId(ingresoId);

        IngresoDTO dto = new IngresoDTO();
        dto.setValor(MONTO);
        dto.setFecha(LocalDate.now());
        dto.setIdUsuario(999L);  // Usuario inexistente

        given(ingresoRepository.findById(ingresoId)).willReturn(Optional.of(ingresoExistente));
        given(usuarioRepository.findById(999L)).willReturn(Optional.empty());

        // ==================== ACT & ASSERT ====================
        assertThatThrownBy(() -> ingresoService.actualizarIngreso(ingresoId, dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuario no encontrado");
    }

    /**
     * Prueba del camino feliz: Eliminación exitosa de ingreso.
     *
     * Tipo de prueba: Funcional positivo (Camino Feliz)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void eliminarIngreso_WithValidId_Success() {
        // ==================== ARRANGE ====================
        Long ingresoId = 1L;

        given(ingresoRepository.existsById(ingresoId)).willReturn(true);

        // ==================== ACT ====================
        ingresoService.eliminarIngreso(ingresoId);

        // ==================== ASSERT ====================
        verify(ingresoRepository).existsById(ingresoId);
        verify(ingresoRepository).deleteById(ingresoId);
    }

    /**
     * Prueba de excepción: Eliminación de ingreso inexistente.
     *
     * Tipo de prueba: Validación negativa (Excepción)
     * Patrón AAA: Arrange, Act, Assert
     */
    @Test
    void eliminarIngreso_WithNonExistentId_ThrowsException() {
        // ==================== ARRANGE ====================
        Long ingresoId = 999L;

        given(ingresoRepository.existsById(ingresoId)).willReturn(false);

        // ==================== ACT & ASSERT ====================
        assertThatThrownBy(() -> ingresoService.eliminarIngreso(ingresoId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ingreso no encontrado");

        verify(ingresoRepository, org.mockito.Mockito.never()).deleteById(ingresoId);
    }
}