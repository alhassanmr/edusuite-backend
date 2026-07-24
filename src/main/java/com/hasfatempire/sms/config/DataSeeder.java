package com.hasfatempire.sms.config;

import com.hasfatempire.sms.model.Role;
import com.hasfatempire.sms.model.School;
import com.hasfatempire.sms.model.User;
import com.hasfatempire.sms.repository.SchoolRepository;
import com.hasfatempire.sms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds demo school & admin account for local development
 * Admin: username "admin" / password "Admin@123"
 * School slug: "demo-school"
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Create demo school if it doesn't exist
        if (!schoolRepository.existsBySlug("demo-school")) {
            School school = School.builder()
                    .name("Demo School")
                    .slug("demo-school")
                    .contactEmail("admin@demoschool.edu")
                    .phone("+233 123 456 7890")
                    .city("Accra")
                    .country("Ghana")
                    .status("ACTIVE")
                    .createdByUsername("system")
                    .build();
            school = schoolRepository.save(school);

            // Create admin user for demo school
            User admin = User.builder()
                    .fullName("Demo Administrator")
                    .username("admin")
                    .email("admin@demoschool.edu")
                    .password(passwordEncoder.encode("Admin@123"))
                    .role(Role.ADMIN)
                    .school(school)
                    .enabled(true)
                    .build();
            userRepository.save(admin);
        }
    }
}
