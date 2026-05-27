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
import com.udea.FinanceTracker.util.JwtBlacklist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Optional;

@Service
public class UsuarioService {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private GoogleTokenVerifier googleTokenVerifier;

    @Autowired
    private UsuarioMapper usuarioMapper;

    @Autowired
    private JwtBlacklist jwtBlacklist;

    /**
     * Authenticate user with Google OAuth2 token
     */
    public AuthenticationResponse authenticateWithGoogle(GoogleLoginRequest request)
            throws GeneralSecurityException, IOException {

        logger.info("Starting Google authentication process");

        // Verify Google token
        Map<String, String> googleUserInfo = null;
        try {
            logger.info("Verifying Google token...");
            googleUserInfo = googleTokenVerifier.verifyToken(request.getIdToken());
            logger.info("Google token verified successfully");
        } catch (GeneralSecurityException e) {
            logger.error("Google security exception: {}", e.getMessage());
            throw e;
        } catch (IOException e) {
            logger.error("Google IO exception: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during Google token verification: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to verify Google token: " + e.getMessage(), e);
        }

        String googleId = googleUserInfo.get("googleId");
        String email = googleUserInfo.get("email");
        String name = googleUserInfo.get("name");

        logger.info("Extracted user info - Email: {}, GoogleId: {}", email, googleId);

        // Check if user exists
        Optional<Usuario> existingUser = usuarioRepository.findByGoogleId(googleId);

        Usuario usuario;
        Boolean isNewUser = false;

        if (existingUser.isPresent()) {
            logger.info("Found existing user with Google ID");
            usuario = existingUser.get();
        } else {
            logger.info("User not found, checking by email...");
            // Check if email exists
            Optional<Usuario> emailUser = usuarioRepository.findByEmail(email);
            if (emailUser.isPresent()) {
                logger.info("Found existing user by email, updating Google ID");
                usuario = emailUser.get();
                usuario.setGoogleId(googleId);
            } else {
                // Create new user
                logger.info("Creating new user");
                usuario = new Usuario(name, email, googleId);
                isNewUser = true;
            }
            usuarioRepository.save(usuario);
            logger.info("User saved successfully");
        }

        // Generate tokens
        logger.info("Generating JWT tokens");
        String accessToken = jwtUtil.generateToken(usuario.getEmail(), usuario.getId());
        String refreshToken = jwtUtil.generateRefreshToken(usuario.getEmail(), usuario.getId());

        UsuarioDTO usuarioDTO = usuarioMapper.toDTO(usuario);

        String message = isNewUser ?
            "New user created. Please complete your profile." :
            "Login successful";

        logger.info("Authentication completed successfully");

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .profileCompleted(usuario.getProfileCompleted())
                .usuario(usuarioDTO)
                .message(message)
                .build();
    }

    /**
     * Validate JWT token
     */
    public Boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }

    /**
     * Extract email from token
     */
    public String getEmailFromToken(String token) {
        return jwtUtil.extractEmail(token);
    }

    /**
     * Get user by email
     */
    public UsuarioDTO getUserByEmail(String email) {
        Optional<Usuario> usuario = usuarioRepository.findByEmail(email);
        return usuario.map(usuarioMapper::toDTO).orElse(null);
    }

    /**
     * Get user by ID
     */
    public UsuarioDTO getUserById(Long id) {
        Optional<Usuario> usuario = usuarioRepository.findById(id);
        return usuario.map(usuarioMapper::toDTO).orElse(null);
    }

    /**
     * Update user profile (complete registration)
     */
    public UsuarioDTO updateUserProfile(Long userId, UpdatePerfilRequest request) throws Exception {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(userId);

        if (!usuarioOpt.isPresent()) {
            throw new Exception("Usuario no encontrado");
        }

        Usuario usuario = usuarioOpt.get();

        if (request.getNombre() != null && !request.getNombre().isBlank()) {
            usuario.setNombre(request.getNombre());
        }
        if (request.getIdGenero() != null) {
            usuario.setIdGenero(request.getIdGenero());
        }
        if (request.getSalario() != null) {
            usuario.setSalario(request.getSalario());
        }
        if (request.getIdOcupacion() != null) {
            usuario.setIdOcupacion(request.getIdOcupacion());
        }
        if (request.getFechaNacimiento() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            java.util.Date parsedDate = dateFormat.parse(request.getFechaNacimiento());
            java.util.Date today = new java.util.Date();

            // Validar que la fecha no sea en el futuro
            if (parsedDate.after(today)) {
                throw new Exception("La fecha de nacimiento no puede ser en el futuro");
            }

            usuario.setFechaNacimiento(parsedDate);
        }

        usuario.setProfileCompleted(true);
        usuario = usuarioRepository.save(usuario);

        return usuarioMapper.toDTO(usuario);
    }

    /**
     * Refresh access token
     */
    public String refreshAccessToken(String refreshToken) throws Exception {
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new Exception("Refresh token is invalid or expired");
        }

        String email = jwtUtil.extractEmail(refreshToken);
        return jwtUtil.generateToken(email);
    }

    /**
     * Create test user (for development/testing without Google OAuth)
     * Remove this in production
     */
    public AuthenticationResponse createTestUser(String email, String name) throws Exception {
        // Check if user exists
        Optional<Usuario> existingUser = usuarioRepository.findByEmail(email);

        Usuario usuario;
        Boolean isNewUser = false;

        if (existingUser.isPresent()) {
            usuario = existingUser.get();
        } else {
            // Create new user
            usuario = new Usuario(name, email, null);
            isNewUser = true;
            usuarioRepository.save(usuario);
        }

        // Generate tokens
        String accessToken = jwtUtil.generateToken(usuario.getEmail(), usuario.getId());
        String refreshToken = jwtUtil.generateRefreshToken(usuario.getEmail(), usuario.getId());

        UsuarioDTO usuarioDTO = usuarioMapper.toDTO(usuario);

        String message = isNewUser ?
            "Test user created. Please complete your profile." :
            "Test login successful";

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .profileCompleted(usuario.getProfileCompleted())
                .usuario(usuarioDTO)
                .message(message)
                .build();
    }

    /**
     * Logout user by blacklisting the access token and optionally the refresh token
     * Since this application uses stateless JWT, logout is handled by token invalidation.
     *
     * @param accessToken access token from Authorization header
     * @param refreshToken optional refresh token to invalidate as well
     */
    public void logout(String accessToken, String refreshToken) {
        if (accessToken != null && !accessToken.trim().isEmpty()) {
            jwtBlacklist.blacklistToken(accessToken);
            logger.info("Access token blacklisted successfully");
        }

        if (refreshToken != null && !refreshToken.trim().isEmpty()) {
            jwtBlacklist.blacklistToken(refreshToken);
            logger.info("Refresh token blacklisted successfully");
        }
    }


    /**
     * Delete user account permanently
     * Validates JWT token, extracts userId from token, and blacklists the token
     * All related data (gasto, presupuesto, etc) will be deleted via CASCADE constraints
     *
     * @param token The JWT token from the Authorization header
     * @return true if deletion was successful
     * @throws IllegalArgumentException if user not found, token invalid, or validation fails
     * @throws IllegalAccessException if the token is already deleted/invalid
     */
    public boolean deleteUser(String token) throws Exception {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token de autenticación requerido");
        }

        try {
            // Validate token format and signature
            if (!jwtUtil.validateToken(token)) {
                throw new IllegalArgumentException("Token JWT inválido o expirado");
            }

            Long userId = null;
            String authenticatedEmail = null;

            try {
                // Extract userId from token
                userId = jwtUtil.extractUserId(token);
                if (userId == null) {
                    throw new IllegalArgumentException("Token no contiene información de usuario válida");
                }

                // Extract email from token
                authenticatedEmail = jwtUtil.extractEmail(token);
                if (authenticatedEmail == null || authenticatedEmail.trim().isEmpty()) {
                    throw new IllegalArgumentException("Token no contiene información de email");
                }

            } catch (io.jsonwebtoken.JwtException e) {
                logger.error("JWT parsing error: {}", e.getMessage());
                throw new IllegalArgumentException("Token JWT malformado o corrupto");
            } catch (Exception e) {
                logger.error("Error extracting claims from token: {}", e.getMessage());
                throw new IllegalArgumentException("No se pudo procesar el token JWT: " + e.getMessage());
            }

            // Find the user to delete
            Optional<Usuario> usuarioOpt = usuarioRepository.findById(userId);

            if (!usuarioOpt.isPresent()) {
                throw new IllegalArgumentException("Usuario no encontrado. Es posible que la cuenta ya haya sido eliminada");
            }

            Usuario usuario = usuarioOpt.get();

            // Validate that the token belongs to the user being deleted
            if (!usuario.getEmail().equals(authenticatedEmail)) {
                logger.warn("Token mismatch: Token email {} does not match user email {}",
                           authenticatedEmail, usuario.getEmail());
                throw new IllegalAccessException("El token no corresponde a este usuario");
            }

            logger.info("Starting account deletion for user: {}", usuario.getEmail());

            try {
                // Delete the user (this will cascade delete all related data)
                usuarioRepository.deleteById(userId);

                logger.info("User deleted successfully: {}", usuario.getEmail());

                // Blacklist the token to prevent further API calls
                jwtBlacklist.blacklistToken(token);
                logger.info("Token blacklisted for user: {}", usuario.getEmail());

                return true;

            } catch (Exception e) {
                logger.error("Error during account deletion for user {}: {}", usuario.getEmail(), e.getMessage());
                throw new Exception("Error al eliminar la cuenta: " + e.getMessage(), e);
            }

        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw e;
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            logger.warn("Expired JWT token used");
            throw new IllegalArgumentException("Token JWT expirado");
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            logger.warn("Malformed JWT token: {}", e.getMessage());
            throw new IllegalArgumentException("Token JWT malformado");
        } catch (io.jsonwebtoken.SignatureException e) {
            logger.warn("Invalid JWT signature");
            throw new IllegalArgumentException("Token JWT inválido (firma incorrecta)");
        } catch (io.jsonwebtoken.JwtException e) {
            logger.warn("JWT exception: {}", e.getMessage());
            throw new IllegalArgumentException("Token JWT inválido: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during token processing: {}", e.getMessage());
            throw new Exception("Error al procesar la solicitud: " + e.getMessage(), e);
        }
    }

}
