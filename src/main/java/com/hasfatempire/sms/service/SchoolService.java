package com.hasfatempire.sms.service;

import com.hasfatempire.sms.dto.SchoolRegistrationRequest;
import com.hasfatempire.sms.exception.BadRequestException;
import com.hasfatempire.sms.exception.ResourceNotFoundException;
import com.hasfatempire.sms.model.Role;
import com.hasfatempire.sms.model.School;
import com.hasfatempire.sms.model.User;
import com.hasfatempire.sms.repository.SchoolRepository;
import com.hasfatempire.sms.repository.UserRepository;
import com.hasfatempire.sms.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SchoolService {

    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * Register a new school with its admin account
     */
    public Map<String, Object> registerSchool(SchoolRegistrationRequest request) {
        // Validate unique slug and email
        if (schoolRepository.existsBySlug(request.slug())) {
            throw new BadRequestException("School slug already taken");
        }
        if (userRepository.existsByUsername(request.adminUsername())) {
            throw new BadRequestException("Admin username already taken");
        }
        if (userRepository.existsByEmail(request.adminEmail())) {
            throw new BadRequestException("Admin email already registered");
        }

        // Create school
        School school = School.builder()
                .name(request.schoolName())
                .slug(request.slug())
                .contactEmail(request.schoolContactEmail())
                .phone(request.schoolPhone())
                .address(request.schoolAddress())
                .city(request.schoolCity())
                .country(request.schoolCountry())
                .status("ACTIVE")
                .createdByUsername(request.adminUsername())
                .build();
        school = schoolRepository.save(school);

        // Create admin user for the school
        User admin = User.builder()
                .fullName(request.adminFullName() != null ? request.adminFullName() : request.adminUsername())
                .username(request.adminUsername())
                .email(request.adminEmail())
                .password(passwordEncoder.encode(request.adminPassword()))
                .role(Role.ADMIN)
                .school(school)
                .enabled(true)
                .build();
        admin = userRepository.save(admin);

        // Generate JWT token
        String token = jwtService.generateToken(admin, claimsFor(admin, school));

        Map<String, Object> response = new HashMap<>();
        response.put("schoolId", school.getId());
        response.put("schoolName", school.getName());
        response.put("schoolSlug", school.getSlug());
        response.put("adminUsername", admin.getUsername());
        response.put("token", token);
        response.put("message", "School registered successfully!");
        return response;
    }

    public School findById(Long id) {
        return schoolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("School not found: " + id));
    }

    public School findBySlug(String slug) {
        return schoolRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("School not found: " + slug));
    }

    public School updateSettings(Long schoolId, School updated) {
        School school = findById(schoolId);
        if (updated.getName() != null) school.setName(updated.getName());
        if (updated.getContactEmail() != null) school.setContactEmail(updated.getContactEmail());
        if (updated.getPhone() != null) school.setPhone(updated.getPhone());
        if (updated.getAddress() != null) school.setAddress(updated.getAddress());
        if (updated.getCity() != null) school.setCity(updated.getCity());
        if (updated.getCountry() != null) school.setCountry(updated.getCountry());
        if (updated.getLogoUrl() != null) school.setLogoUrl(updated.getLogoUrl());
        if (updated.getTheme() != null) school.setTheme(updated.getTheme());
        return schoolRepository.save(school);
    }

    private Map<String, Object> claimsFor(User user, School school) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        claims.put("fullName", user.getFullName());
        claims.put("userId", user.getId());
        claims.put("schoolId", school.getId());
        claims.put("schoolName", school.getName());
        claims.put("schoolSlug", school.getSlug());
        return claims;
    }
}
