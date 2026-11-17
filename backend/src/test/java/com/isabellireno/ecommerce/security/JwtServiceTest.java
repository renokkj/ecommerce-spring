package com.isabellireno.ecommerce.security;

import com.isabellireno.ecommerce.config.AppProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static AppProperties props(String secret, long minutes) {
        return new AppProperties("http://localhost:8080", new AppProperties.Jwt(secret, minutes),
                null, null, null, null);
    }

    private final JwtService jwtService = new JwtService(props("chave-de-teste-com-mais-de-32-bytes-123456", 60));

    @Test
    void validTokenReturnsSubject() {
        String token = jwtService.generateToken("ana@teste.com", "CUSTOMER");

        assertThat(jwtService.extractValidSubject(token)).contains("ana@teste.com");
    }

    @Test
    void tamperedTokenIsRejected() {
        String token = jwtService.generateToken("ana@teste.com", "CUSTOMER");
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertThat(jwtService.extractValidSubject(tampered)).isEmpty();
    }

    @Test
    void tokenSignedWithOtherKeyIsRejected() {
        JwtService other = new JwtService(props("outra-chave-de-teste-com-mais-de-32-bytes", 60));
        String token = other.generateToken("ana@teste.com", "CUSTOMER");

        assertThat(jwtService.extractValidSubject(token)).isEmpty();
    }

    @Test
    void shortSecretIsRefused() {
        assertThatThrownBy(() -> new JwtService(props("curta", 60))).isInstanceOf(IllegalStateException.class);
    }
}
