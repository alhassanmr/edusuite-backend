package com.hasfatempire.sms.controller;

import com.hasfatempire.sms.exception.ResourceNotFoundException;
import com.hasfatempire.sms.model.School;
import com.hasfatempire.sms.model.User;
import com.hasfatempire.sms.repository.UserRepository;
import com.hasfatempire.sms.service.SchoolService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/school")
@RequiredArgsConstructor
public class SchoolController {

    private final SchoolService schoolService;
    private final UserRepository userRepository;

    /**
     * Get current school settings (for authenticated user's school)
     */
    @GetMapping("/settings")
    public ResponseEntity<School> getSettings(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(user.getSchool());
    }

    /**
     * Update school settings (admin only)
     */
    @PutMapping("/settings")
    public ResponseEntity<School> updateSettings(@Valid @RequestBody School updatedSchool, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.getRole().name().equals("ADMIN")) {
            throw new IllegalArgumentException("Only admins can update school settings");
        }

        School updated = schoolService.updateSettings(user.getSchool().getId(), updatedSchool);
        return ResponseEntity.ok(updated);
    }

    /**
     * Get school by slug (public — for client to fetch school branding)
     */
    @GetMapping("/public/{slug}")
    public ResponseEntity<School> getBySlug(@PathVariable String slug) {
        School school = schoolService.findBySlug(slug);
        return ResponseEntity.ok(school);
    }
}
