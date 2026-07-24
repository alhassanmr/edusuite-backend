package com.hasfatempire.sms.config;

import com.hasfatempire.sms.model.Role;
import com.hasfatempire.sms.model.User;
import com.hasfatempire.sms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds a default admin account on first boot (dev profile) so the app is usable
 * immediately: username "admin" / password "Admin@123".
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .fullName("System Administrator")
                    .username("admin")
                    .email("admin@hasfatempire.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .role(Role.ADMIN)
                    .enabled(true)
                    .build();
            userRepository.save(admin);
        }
    }
}
