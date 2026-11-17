package com.isabellireno.ecommerce.user;

import com.isabellireno.ecommerce.common.BusinessException;
import com.isabellireno.ecommerce.security.JwtService;
import com.isabellireno.ecommerce.user.AuthDtos.AuthResponse;
import com.isabellireno.ecommerce.user.AuthDtos.LoginRequest;
import com.isabellireno.ecommerce.user.AuthDtos.RegisterRequest;
import com.isabellireno.ecommerce.user.AuthDtos.UserResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("Já existe uma conta com este e-mail.");
        }
        User user = userRepository.save(new User(request.name().trim(), email,
                passwordEncoder.encode(request.password()), Role.CUSTOMER));
        return toAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().trim().toLowerCase(), request.password()));
        return toAuthResponse((User) auth.getPrincipal());
    }

    private AuthResponse toAuthResponse(User user) {
        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(token, "Bearer", jwtService.getExpirationMinutes(), UserResponse.from(user));
    }
}
