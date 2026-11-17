package com.isabellireno.ecommerce.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(
            @NotBlank(message = "Informe o nome.") @Size(max = 120) String name,
            @NotBlank(message = "Informe o e-mail.") @Email(message = "E-mail inválido.") String email,
            @NotBlank @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres.") String password
    ) {}

    public record LoginRequest(
            @NotBlank(message = "Informe o e-mail.") @Email String email,
            @NotBlank(message = "Informe a senha.") String password
    ) {}

    public record UserResponse(Long id, String name, String email, Role role) {
        public static UserResponse from(User user) {
            return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole());
        }
    }

    public record AuthResponse(String token, String tokenType, long expiresInMinutes, UserResponse user) {}
}
