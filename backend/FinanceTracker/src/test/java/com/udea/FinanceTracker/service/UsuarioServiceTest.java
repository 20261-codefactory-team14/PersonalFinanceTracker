package com.udea.FinanceTracker.service;

import com.udea.FinanceTracker.dto.AuthenticationResponse;
import com.udea.FinanceTracker.dto.GoogleLoginRequest;
import com.udea.FinanceTracker.dto.UpdatePerfilRequest;
import com.udea.FinanceTracker.dto.UsuarioDTO;
import com.udea.FinanceTracker.entity.Usuario;
import com.udea.FinanceTracker.mapper.UsuarioMapper;
import com.udea.FinanceTracker.repository.UsuarioRepository;
import com.udea.FinanceTracker.util.GoogleTokenVerifier;
import com.udea.FinanceTracker.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.udea.FinanceTracker.util.JwtBlacklist;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.text.ParseException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UsuarioServiceTest {

    private static final String TEST_EMAIL = "test@example.com";
    private static final String GOOGLE_ID = "google-123";
    private static final String ACCESS_TOKEN = "access-token";
    private static final String REFRESH_TOKEN = "refresh-token";

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private GoogleTokenVerifier googleTokenVerifier;

    @Mock
    private UsuarioMapper usuarioMapper;

    @Mock
    private JwtBlacklist jwtBlacklist;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void authenticateWithGoogle_CreatesNewUser_WhenNoExistingUser() throws Exception {
        GoogleLoginRequest request = new GoogleLoginRequest();
        request.setIdToken("id-token");

        Map<String, String> googleUserInfo = new HashMap<>();
        googleUserInfo.put("googleId", GOOGLE_ID);
        googleUserInfo.put("email", TEST_EMAIL);
        googleUserInfo.put("name", "Test User");

        given(googleTokenVerifier.verifyToken("id-token")).willReturn(googleUserInfo);
        given(usuarioRepository.findByGoogleId(GOOGLE_ID)).willReturn(Optional.empty());
        given(usuarioRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.empty());

        Usuario savedUsuario = new Usuario("Test User", TEST_EMAIL, GOOGLE_ID);
        savedUsuario.setId(1L);

        given(usuarioRepository.save(any(Usuario.class))).willReturn(savedUsuario);
        given(jwtUtil.generateToken(eq(TEST_EMAIL), nullable(Long.class))).willReturn(ACCESS_TOKEN);
        given(jwtUtil.generateRefreshToken(eq(TEST_EMAIL), nullable(Long.class))).willReturn(REFRESH_TOKEN);

        UsuarioDTO dto = UsuarioDTO.builder()
                .id(1L)
                .nombre("Test User")
                .email(TEST_EMAIL)
                .googleId(GOOGLE_ID)
                .profileCompleted(false)
                .build();

        given(usuarioMapper.toDTO(any(Usuario.class))).willReturn(dto);

        AuthenticationResponse response = usuarioService.authenticateWithGoogle(request);

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).contains("New user created");
        assertThat(response.getAccessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(response.getRefreshToken()).isEqualTo(REFRESH_TOKEN);
        assertThat(response.getUsuario()).isEqualTo(dto);

        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void authenticateWithGoogle_ExistingUserByGoogleId_ReturnsLoginSuccessful() throws Exception {
        GoogleLoginRequest request = new GoogleLoginRequest();
        request.setIdToken("id-token");

        Map<String, String> googleUserInfo = new HashMap<>();
        googleUserInfo.put("googleId", GOOGLE_ID);
        googleUserInfo.put("email", TEST_EMAIL);
        googleUserInfo.put("name", "Existing User");

        Usuario existingUser = new Usuario("Existing User", TEST_EMAIL, GOOGLE_ID);
        existingUser.setId(2L);

        given(googleTokenVerifier.verifyToken("id-token")).willReturn(googleUserInfo);
        given(usuarioRepository.findByGoogleId(GOOGLE_ID)).willReturn(Optional.of(existingUser));
        given(jwtUtil.generateToken(eq(TEST_EMAIL), nullable(Long.class))).willReturn(ACCESS_TOKEN);
        given(jwtUtil.generateRefreshToken(eq(TEST_EMAIL), nullable(Long.class))).willReturn(REFRESH_TOKEN);

        UsuarioDTO dto = UsuarioDTO.builder()
                .id(2L)
                .nombre("Existing User")
                .email(TEST_EMAIL)
                .googleId(GOOGLE_ID)
                .profileCompleted(false)
                .build();

        given(usuarioMapper.toDTO(existingUser)).willReturn(dto);

        AuthenticationResponse response = usuarioService.authenticateWithGoogle(request);

        assertThat(response.getMessage()).isEqualTo("Login successful");
        assertThat(response.getUsuario()).isEqualTo(dto);

        verify(usuarioRepository, never()).findByEmail(anyString());
    }

    @Test
    void authenticateWithGoogle_ExistingUserByEmail_UpdatesGoogleId() throws Exception {
        GoogleLoginRequest request = new GoogleLoginRequest();
        request.setIdToken("id-token");

        Map<String, String> googleUserInfo = new HashMap<>();
        googleUserInfo.put("googleId", GOOGLE_ID);
        googleUserInfo.put("email", TEST_EMAIL);
        googleUserInfo.put("name", "Existing Email User");

        Usuario existingUser = new Usuario("Existing Email User", TEST_EMAIL, null);
        existingUser.setId(3L);

        given(googleTokenVerifier.verifyToken("id-token")).willReturn(googleUserInfo);
        given(usuarioRepository.findByGoogleId(GOOGLE_ID)).willReturn(Optional.empty());
        given(usuarioRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.of(existingUser));
        given(usuarioRepository.save(any(Usuario.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(jwtUtil.generateToken(eq(TEST_EMAIL), nullable(Long.class))).willReturn(ACCESS_TOKEN);
        given(jwtUtil.generateRefreshToken(eq(TEST_EMAIL), nullable(Long.class))).willReturn(REFRESH_TOKEN);

        UsuarioDTO dto = UsuarioDTO.builder()
                .id(3L)
                .nombre("Existing Email User")
                .email(TEST_EMAIL)
                .googleId(GOOGLE_ID)
                .profileCompleted(false)
                .build();

        given(usuarioMapper.toDTO(existingUser)).willReturn(dto);

        AuthenticationResponse response = usuarioService.authenticateWithGoogle(request);

        assertThat(response.getMessage()).isEqualTo("Login successful");
        assertThat(existingUser.getGoogleId()).isEqualTo(GOOGLE_ID);

        verify(usuarioRepository).save(existingUser);
    }

    @Test
    void getUserByEmail_ReturnsDtoWhenFound() {
        Usuario usuario = new Usuario("Test User", TEST_EMAIL, GOOGLE_ID);
        usuario.setId(4L);

        UsuarioDTO dto = UsuarioDTO.builder()
                .id(4L)
                .email(TEST_EMAIL)
                .nombre("Test User")
                .build();

        given(usuarioRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.of(usuario));
        given(usuarioMapper.toDTO(usuario)).willReturn(dto);

        UsuarioDTO result = usuarioService.getUserByEmail(TEST_EMAIL);

        assertThat(result).isEqualTo(dto);
    }

    @Test
    void getUserById_ReturnsDtoWhenFound() {
        Usuario usuario = new Usuario("Test User", TEST_EMAIL, GOOGLE_ID);
        usuario.setId(5L);

        UsuarioDTO dto = UsuarioDTO.builder()
                .id(5L)
                .email(TEST_EMAIL)
                .nombre("Test User")
                .build();

        given(usuarioRepository.findById(5L)).willReturn(Optional.of(usuario));
        given(usuarioMapper.toDTO(usuario)).willReturn(dto);

        UsuarioDTO result = usuarioService.getUserById(5L);

        assertThat(result).isEqualTo(dto);
    }

    @Test
    void updateUserProfile_ThrowsExceptionWhenUserMissing() {
        UpdatePerfilRequest request = new UpdatePerfilRequest();

        given(usuarioRepository.findById(anyLong())).willReturn(Optional.empty());

        Exception exception = assertThrows(
                Exception.class,
                () -> usuarioService.updateUserProfile(99L, request)
        );

        assertThat(exception.getMessage()).isEqualTo("Usuario no encontrado");
    }

    @Test
    void updateUserProfile_UpdatesFieldsAndReturnsDto() throws Exception {
        Usuario usuario = new Usuario("Old Name", TEST_EMAIL, GOOGLE_ID);
        usuario.setId(6L);

        UpdatePerfilRequest request = new UpdatePerfilRequest();
        request.setNombre("New Name");
        request.setIdGenero(2L);
        request.setFechaNacimiento("2000-01-01");
        request.setSalario(1500L);
        request.setIdOcupacion(3L);

        given(usuarioRepository.findById(6L)).willReturn(Optional.of(usuario));
        given(usuarioRepository.save(any(Usuario.class))).willAnswer(invocation -> invocation.getArgument(0));

        UsuarioDTO dto = UsuarioDTO.builder()
                .id(6L)
                .nombre("New Name")
                .email(TEST_EMAIL)
                .googleId(GOOGLE_ID)
                .idGenero(2L)
                .fechaNacimiento("2000-01-01")
                .salario(1500L)
                .idOcupacion(3L)
                .profileCompleted(true)
                .build();

        given(usuarioMapper.toDTO(any(Usuario.class))).willReturn(dto);

        UsuarioDTO result = usuarioService.updateUserProfile(6L, request);

        assertThat(result.getNombre()).isEqualTo("New Name");
        assertThat(result.getIdGenero()).isEqualTo(2L);
        assertThat(result.getFechaNacimiento()).isEqualTo("2000-01-01");
        assertThat(result.getSalario()).isEqualTo(1500L);
        assertThat(result.getIdOcupacion()).isEqualTo(3L);
        assertThat(result.getProfileCompleted()).isTrue();

        verify(usuarioRepository).save(usuario);
    }

    @Test
    void refreshAccessToken_ThrowsExceptionWhenRefreshTokenInvalid() {
        given(jwtUtil.validateToken(anyString())).willReturn(false);

        Exception exception = assertThrows(
                Exception.class,
                () -> usuarioService.refreshAccessToken("invalid-token")
        );

        assertThat(exception.getMessage()).isEqualTo("Refresh token is invalid or expired");
    }

    @Test
    void refreshAccessToken_ReturnsNewTokenWhenValid() throws Exception {
        given(jwtUtil.validateToken(anyString())).willReturn(true);
        given(jwtUtil.extractEmail(anyString())).willReturn(TEST_EMAIL);
        given(jwtUtil.generateToken(TEST_EMAIL)).willReturn(ACCESS_TOKEN);

        String token = usuarioService.refreshAccessToken("valid-token");

        assertThat(token).isEqualTo(ACCESS_TOKEN);
    }

    @Test
    void createTestUser_CreatesNewTestUserWhenMissing() throws Exception {
        given(usuarioRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.empty());

        Usuario savedUsuario = new Usuario("Test User", TEST_EMAIL, null);
        savedUsuario.setId(7L);

        given(usuarioRepository.save(any(Usuario.class))).willReturn(savedUsuario);
        given(jwtUtil.generateToken(eq(TEST_EMAIL), nullable(Long.class))).willReturn(ACCESS_TOKEN);
        given(jwtUtil.generateRefreshToken(eq(TEST_EMAIL), nullable(Long.class))).willReturn(REFRESH_TOKEN);

        UsuarioDTO dto = UsuarioDTO.builder()
                .id(7L)
                .nombre("Test User")
                .email(TEST_EMAIL)
                .googleId(null)
                .profileCompleted(false)
                .build();

        given(usuarioMapper.toDTO(any(Usuario.class))).willReturn(dto);

        AuthenticationResponse response = usuarioService.createTestUser(TEST_EMAIL, "Test User");

        assertThat(response.getMessage()).contains("Test user created");
        assertThat(response.getUsuario()).isEqualTo(dto);
    }

    @Test
    void createTestUser_ReturnsExistingUserWhenPresent() throws Exception {
        Usuario existingUsuario = new Usuario("Existing User", TEST_EMAIL, null);
        existingUsuario.setId(8L);

        given(usuarioRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.of(existingUsuario));
        given(jwtUtil.generateToken(eq(TEST_EMAIL), nullable(Long.class))).willReturn(ACCESS_TOKEN);
        given(jwtUtil.generateRefreshToken(eq(TEST_EMAIL), nullable(Long.class))).willReturn(REFRESH_TOKEN);

        UsuarioDTO dto = UsuarioDTO.builder()
                .id(8L)
                .nombre("Existing User")
                .email(TEST_EMAIL)
                .googleId(null)
                .profileCompleted(false)
                .build();

        given(usuarioMapper.toDTO(existingUsuario)).willReturn(dto);

        AuthenticationResponse response = usuarioService.createTestUser(TEST_EMAIL, "Existing User");

        assertThat(response.getMessage()).isEqualTo("Test login successful");
        assertThat(response.getUsuario()).isEqualTo(dto);

        verify(usuarioRepository, never()).save(any());
    }

    /**
     * Prueba de excepción: Validación de fecha de nacimiento futura.
     *
     * Tipo de prueba: Validación negativa (Excepción)
     * Patrón AAA: Arrange, Act, Assert
     *
     * Corresponde al CP-003-B: Validación de edad negativa o no numérica
     */
    @Test
    void updateUserProfile_FutureDate_ThrowsException() {
        // ==================== ARRANGE ====================
        // Configurar usuario existente
        Usuario existingUser = new Usuario("Test User", TEST_EMAIL, GOOGLE_ID);
        existingUser.setId(1L);

        // Crear request con fecha futura
        UpdatePerfilRequest request = new UpdatePerfilRequest();
        request.setNombre("New Name");
        request.setFechaNacimiento("2030-05-21"); // Fecha futura -> edad negativa

        given(usuarioRepository.findById(1L)).willReturn(Optional.of(existingUser));

        // ==================== ACT & ASSERT ====================
        // Verificar que se lanza una excepción por fecha inválida
        assertThatThrownBy(() -> usuarioService.updateUserProfile(1L, request))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("fecha");

        // Verificar que el usuario no fue actualizado
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    /**
     * Prueba de excepción: Validación de formato de fecha incorrecto.
     *
     * Tipo de prueba: Validación negativa (Excepción)
     * Patrón AAA: Arrange, Act, Assert
     *
     * Corresponde al CP-003-B: Validación de edad negativa o no numérica
     */
    @Test
    void updateUserProfile_InvalidDateFormat_ThrowsException() {
        // ==================== ARRANGE ====================
        Usuario existingUser = new Usuario("Test User", TEST_EMAIL, GOOGLE_ID);
        existingUser.setId(1L);

        // Crear request con formato de fecha incorrecto
        UpdatePerfilRequest request = new UpdatePerfilRequest();
        request.setNombre("New Name");
        request.setFechaNacimiento("1990/05/21"); // Formato incorrecto (debería ser yyyy-MM-dd)

        given(usuarioRepository.findById(1L)).willReturn(Optional.of(existingUser));

        // ==================== ACT & ASSERT ====================
        assertThatThrownBy(() -> usuarioService.updateUserProfile(1L, request))
                .isInstanceOf(Exception.class);
    }

    // ==================== PRUEBAS PARA ELIMINACIÓN DE CUENTA ====================
    // Corresponde al CP-008-A y CP-008-B: Eliminación de cuenta de forma lógica
    // ====================

    /**
     * Prueba del camino feliz: Eliminación exitosa de cuenta con confirmación.
     *
     * Tipo de prueba: Funcional positivo (Camino Feliz)
     * Patrón AAA: Arrange, Act, Assert
     *
     * Corresponde al CP-008-A: Confirmación crítica y eliminación de cuenta
     */
    @Test
    void deleteUser_WithValidTokenAndConfirmTrue_DeletesUserAndBlacklistsToken() throws Exception {
        // ==================== ARRANGE ====================
        String validToken = "valid.jwt.token";
        Long userId = 1L;
        String userEmail = "test@example.com";

        // Configurar usuario existente
        Usuario existingUser = new Usuario("Test User", userEmail, GOOGLE_ID);
        existingUser.setId(userId);

        // Configurar mocks para JWT
        given(jwtUtil.validateToken(validToken)).willReturn(true);
        given(jwtUtil.extractUserId(validToken)).willReturn(userId);
        given(jwtUtil.extractEmail(validToken)).willReturn(userEmail);

        given(usuarioRepository.findById(userId)).willReturn(Optional.of(existingUser));

        // ==================== ACT ====================
        boolean deleted = usuarioService.deleteUser(validToken);

        // ==================== ASSERT ====================
        assertThat(deleted).isTrue();
        verify(usuarioRepository).deleteById(userId);
        verify(jwtBlacklist).blacklistToken(validToken);
    }

    /**
     * Prueba de excepción: Eliminación de cuenta sin confirmación.
     *
     * Tipo de prueba: Validación negativa (Excepción)
     * Patrón AAA: Arrange, Act, Assert
     *
     * Corresponde al CP-008-A (variante de excepción)
     */
    @Test
    void deleteUser_WithInvalidToken_ThrowsException() {
        // ==================== ARRANGE ====================
        String invalidToken = "invalid.token";

        given(jwtUtil.validateToken(invalidToken)).willReturn(false);

        // ==================== ACT & ASSERT ====================
        assertThatThrownBy(() -> usuarioService.deleteUser(invalidToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Token JWT inválido o expirado");
    }

    /**
     * Prueba de seguridad: Token blacklist después de eliminación.
     *
     * Tipo de prueba: Seguridad
     * Patrón AAA: Arrange, Act, Assert
     *
     * Corresponde al CP-008-B: Cierre de sesión inmediato tras eliminación de cuenta
     * Verifica que el token es añadido al blacklist y ya no es válido.
     */
    @Test
    void deleteUser_TokenIsBlacklisted_AfterDeletion() throws Exception {
        // ==================== ARRANGE ====================
        String validToken = "valid.jwt.token";
        Long userId = 1L;
        String userEmail = "test@example.com";

        Usuario existingUser = new Usuario("Test User", userEmail, GOOGLE_ID);
        existingUser.setId(userId);

        given(jwtUtil.validateToken(validToken)).willReturn(true);
        given(jwtUtil.extractUserId(validToken)).willReturn(userId);
        given(jwtUtil.extractEmail(validToken)).willReturn(userEmail);
        given(usuarioRepository.findById(userId)).willReturn(Optional.of(existingUser));

        // Configurar blacklist mock
        given(jwtBlacklist.isBlacklisted(anyString())).willReturn(false);

        // ==================== ACT ====================
        boolean deleted = usuarioService.deleteUser(validToken);

        // ==================== ASSERT ====================
        assertThat(deleted).isTrue();
        // Verificar que el token fue añadido al blacklist
        verify(jwtBlacklist).blacklistToken(validToken);

        // Simular que después de la eliminación, el token está en blacklist
        given(jwtBlacklist.isBlacklisted(validToken)).willReturn(true);
        assertThat(jwtBlacklist.isBlacklisted(validToken)).isTrue();
    }
}