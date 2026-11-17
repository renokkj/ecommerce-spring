package com.isabellireno.ecommerce.user;

import com.isabellireno.ecommerce.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cria o usuário administrador a partir das variáveis ADMIN_EMAIL e ADMIN_PASSWORD,
 * para que nenhuma senha fique gravada no repositório.
 */
@Component
public class AdminUserInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminUserInitializer.class);

    private final AppProperties properties;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserInitializer(AppProperties properties, UserRepository userRepository,
                                PasswordEncoder passwordEncoder) {
        this.properties = properties;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String email = properties.admin().email();
        String password = properties.admin().password();
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            log.info("ADMIN_EMAIL/ADMIN_PASSWORD não definidos: nenhum administrador criado.");
            return;
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            return;
        }
        userRepository.save(new User("Administrador", email.trim().toLowerCase(),
                passwordEncoder.encode(password), Role.ADMIN));
        log.info("Usuário administrador criado: {}", email);
    }
}
