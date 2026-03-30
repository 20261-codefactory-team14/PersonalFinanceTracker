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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
        String accessToken = jwtUtil.generateToken(usuario.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(usuario.getEmail());

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
            usuario.setFechaNacimiento(dateFormat.parse(request.getFechaNacimiento()));
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
        String accessToken = jwtUtil.generateToken(usuario.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(usuario.getEmail());

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
}

