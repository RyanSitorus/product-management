package com.assessment.product.security;

import com.assessment.product.entity.Role;
import com.assessment.product.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;
    private final String secret = "9a67279f10b32531a3e205f1299c0a481c1ae04c3519db83042183223a91932341f043e25a4741c5c9fa459f3abff9f7f5e57a5d738125431e477436d009a610";
    private final long expirationMs = 3600000;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(tokenProvider, "jwtSecret", secret);
        ReflectionTestUtils.setField(tokenProvider, "jwtExpirationMs", expirationMs);
        tokenProvider.init();
    }

    @Test
    @DisplayName("generateToken() and getUsernameFromToken() - Success")
    void generateToken_Success() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .role(Role.ROLE_USER)
                .build();
        CustomUserDetails userDetails = new CustomUserDetails(user);
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        String token = tokenProvider.generateToken(auth);

        assertThat(token).isNotBlank();
        assertThat(tokenProvider.validateToken(token)).isTrue();
        assertThat(tokenProvider.getUsernameFromToken(token)).isEqualTo("testuser");
    }

    @Test
    @DisplayName("generateTokenFromUsername() - Success")
    void generateTokenFromUsername_Success() {
        String token = tokenProvider.generateTokenFromUsername("adminuser", "ROLE_ADMIN", 2L, "admin@example.com");

        assertThat(token).isNotBlank();
        assertThat(tokenProvider.validateToken(token)).isTrue();
        assertThat(tokenProvider.getUsernameFromToken(token)).isEqualTo("adminuser");
        assertThat(tokenProvider.getExpirationMs()).isEqualTo(expirationMs);
    }

    @Test
    @DisplayName("validateToken() - Returns false for invalid token")
    void validateToken_Invalid() {
        assertThat(tokenProvider.validateToken("invalid.token.string")).isFalse();
        assertThat(tokenProvider.validateToken("")).isFalse();
    }
}
