package com.hasfatempire.sms.controller;

import com.hasfatempire.sms.dto.AuthRequest;
import com.hasfatempire.sms.dto.AuthResponse;
import com.hasfatempire.sms.dto.SchoolRegistrationRequest;
import com.hasfatempire.sms.exception.BadRequestException;
import com.hasfatempire.sms.model.User;
import com.hasfatempire.sms.repository.UserRepository;
import com.hasfatempire.sms.security.JwtService;
import com.hasfatempire.sms.service.SchoolService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final SchoolService schoolService;

    /**
     * School registration — creates new school + admin account
     */
    @PostMapping("/schools/register")
    public ResponseEntity<Map<String, Object>> registerSchool(@Valid @RequestBody SchoolRegistrationRequest request) {
        Map<String, Object> response = schoolService.registerSchool(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Login to a school
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new BadRequestException("User not found"));

        String token = jwtService.generateToken(user, claimsFor(user));

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("username", user.getUsername());
        response.put("fullName", user.getFullName());
        response.put("role", user.getRole().name());
        response.put("schoolId", user.getSchool().getId());
        response.put("schoolName", user.getSchool().getName());
        response.put("schoolSlug", user.getSchool().getSlug());
        return ResponseEntity.ok(response);
    }

    private Map<String, Object> claimsFor(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        claims.put("fullName", user.getFullName());
        claims.put("userId", user.getId());
        claims.put("schoolId", user.getSchool().getId());
        claims.put("schoolName", user.getSchool().getName());
        claims.put("schoolSlug", user.getSchool().getSlug());
        return claims;
    }
}
