package com.udea.FinanceTracker.config;

import com.udea.FinanceTracker.service.UsuarioService;
import com.udea.FinanceTracker.util.JwtUtil;
import com.udea.FinanceTracker.util.JwtBlacklist;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final JwtBlacklist jwtBlacklist;

    public JwtAuthFilter(JwtUtil jwtUtil, JwtBlacklist jwtBlacklist) {
        this.jwtUtil = jwtUtil;
        this.jwtBlacklist = jwtBlacklist;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                // Check if token is blacklisted (user deleted their account)
                if (jwtBlacklist.isBlacklisted(token)) {
                    // Token is blacklisted, don't authenticate
                    filterChain.doFilter(request, response);
                    return;
                }

                if (jwtUtil.validateToken(token)) {
                    String email = jwtUtil.extractEmail(token);

                    // Le dice a Spring Security que este request está autenticado
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(email, null, List.of());

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception ignored) {
                // Token inválido, Spring Security bloqueará el acceso
            }
        }

        filterChain.doFilter(request, response);
    }
}