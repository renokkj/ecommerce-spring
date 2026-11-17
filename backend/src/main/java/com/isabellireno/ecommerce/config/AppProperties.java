package com.isabellireno.ecommerce.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        String publicBaseUrl,
        Jwt jwt,
        Admin admin,
        Storage storage,
        Cors cors,
        Payment payment
) {
    public record Jwt(String secret, long expirationMinutes) {}

    public record Admin(String email, String password) {}

    public record Storage(String uploadDir) {}

    public record Cors(List<String> allowedOrigins) {}

    public record Payment(String mercadopagoAccessToken, String frontendUrl) {
        public boolean mercadoPagoEnabled() {
            return mercadopagoAccessToken != null && !mercadopagoAccessToken.isBlank();
        }
    }
}
