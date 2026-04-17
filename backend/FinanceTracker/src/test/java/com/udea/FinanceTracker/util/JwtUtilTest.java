package com.udea.FinanceTracker.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secretKey", "01234567890123456789012345678901");
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", 60000L);
        ReflectionTestUtils.setField(jwtUtil, "refreshTokenExpiration", 120000L);
    }

    @Test
    void generateTokenAndExtractEmail_ReturnsEmailAndValidatesToken() {
        String email = "user@example.com";

        String token = jwtUtil.generateToken(email);

        assertThat(token).isNotBlank();
        assertThat(jwtUtil.extractEmail(token)).isEqualTo(email);
        assertThat(jwtUtil.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_ReturnsFalseForInvalidToken() {
        assertThat(jwtUtil.validateToken("invalid.token.value")).isFalse();
    }

    @Test
    void validateTokenWithEmail_ReturnsTrueForMatchingEmail() {
        String email = "user@example.com";
        String token = jwtUtil.generateToken(email);

        assertThat(jwtUtil.validateToken(token, email)).isTrue();
    }

    @Test
    void validateTokenWithEmail_ReturnsFalseForNonMatchingEmail() {
        String token = jwtUtil.generateToken("user@example.com");

        assertThat(jwtUtil.validateToken(token, "other@example.com")).isFalse();
    }
}
