package com.library.api.config;

import com.library.api.repository.UserRepository;
import com.library.api.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Creates the first admin from configuration, since registration only ever creates regular users.
 * Set APP_ADMIN_USERNAME and APP_ADMIN_PASSWORD (or app.admin.username and app.admin.password).
 */
@Component
public class AdminSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final AuthService authService;
    private final UserRepository userRepository;
    private final String username;
    private final String password;

    public AdminSeeder(AuthService authService, UserRepository userRepository,
                       @Value("${app.admin.username:}") String username,
                       @Value("${app.admin.password:}") String password) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.username = username;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (username.isBlank() || password.isBlank()) {
            log.info("No admin configured, set app.admin.username and app.admin.password to create one.");
            return;
        }
        if (userRepository.findByUsername(username).isEmpty()) {
            authService.createAdmin(username, password);
            log.info("Created admin user '{}'.", username);
        }
    }
}
