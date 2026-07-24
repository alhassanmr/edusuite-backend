package com.hasfatempire.sms.controller;

import com.hasfatempire.sms.dto.InviteUserRequest;
import com.hasfatempire.sms.exception.BadRequestException;
import com.hasfatempire.sms.exception.ResourceNotFoundException;
import com.hasfatempire.sms.model.Role;
import com.hasfatempire.sms.model.School;
import com.hasfatempire.sms.model.User;
import com.hasfatempire.sms.notification.NotificationService;
import com.hasfatempire.sms.repository.UserRepository;
import com.hasfatempire.sms.security.TenantContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin invites parents, students, and teachers to the platform.
 * Creates a login account linked to their record + sends credentials via SMS/email.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserManagementController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TenantContext tenantContext;
    private final NotificationService notificationService;

    /** List all user accounts in my school */
    @GetMapping
    public List<User> all(Authentication auth) {
        return userRepository.findBySchoolId(tenantContext.getCurrentSchoolId(auth));
    }

    /**
     * Invite a parent, student, or teacher.
     * Creates their account and sends login credentials via SMS and/or email.
     */
    @PostMapping("/invite")
    public ResponseEntity<Map<String, Object>> invite(@Valid @RequestBody InviteUserRequest request,
                                                       Authentication auth) {
        School school = tenantContext.getCurrentSchool(auth);

        if (userRepository.existsByUsername(request.username())) {
            throw new BadRequestException("Username already taken");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email already registered");
        }

        Role role;
        try {
            role = Role.valueOf(request.role().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Role must be PARENT, STUDENT, or TEACHER");
        }
        if (role == Role.ADMIN) {
            throw new BadRequestException("Use school settings to add administrators");
        }

        User.UserBuilder builder = User.builder()
                .fullName(request.fullName())
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.temporaryPassword()))
                .role(role)
                .school(school)
                .enabled(true);

        // Link account to their parent/student/teacher record
        switch (role) {
            case PARENT -> builder.linkedParentId(request.linkedRecordId());
            case STUDENT -> builder.linkedStudentId(request.linkedRecordId());
            case TEACHER -> builder.linkedTeacherId(request.linkedRecordId());
            default -> {}
        }

        User user = userRepository.save(builder.build());

        // Send credentials
        String message = String.format(
                "Welcome to %s on EduSuite! Login at the school portal. Username: %s, Temporary password: %s. Please change your password after first login.",
                school.getName(), request.username(), request.temporaryPassword());

        if (request.sendSms() && request.phone() != null) {
            notificationService.sendSms(school, request.phone(), message);
        }
        if (request.sendEmail()) {
            notificationService.sendEmail(school, request.email(),
                    "Your " + school.getName() + " portal account", message);
        }

        return ResponseEntity.ok(Map.of(
                "userId", user.getId(),
                "username", user.getUsername(),
                "role", user.getRole().name(),
                "message", "Account created. Credentials " +
                        ((request.sendSms() || request.sendEmail()) ? "sent." : "NOT sent (both channels off).")
        ));
    }

    /** Disable a user account */
    @PostMapping("/{id}/disable")
    public ResponseEntity<Void> disable(@PathVariable Long id, Authentication auth) {
        Long schoolId = tenantContext.getCurrentSchoolId(auth);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!user.getSchool().getId().equals(schoolId)) {
            throw new ResourceNotFoundException("User not found");
        }
        user.setEnabled(false);
        userRepository.save(user);
        return ResponseEntity.noContent().build();
    }
}
