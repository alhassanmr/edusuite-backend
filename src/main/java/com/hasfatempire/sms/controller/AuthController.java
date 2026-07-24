package com.hasfatempire.sms.controller;

import com.hasfatempire.sms.dto.AuthRequest;
import com.hasfatempire.sms.dto.AuthResponse;
import com.hasfatempire.sms.dto.RegisterRequest;
import com.hasfatempire.sms.exception.BadRequestException;
import com.hasfatempire.sms.model.Role;
import com.hasfatempire.sms.model.User;
import com.hasfatempire.sms.repository.UserRepository;
import com.hasfatempire.sms.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
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
            throw new BadRequestException("Invalid role. Must be one of ADMIN, TEACHER, PARENT, STUDENT");
        }

        User user = User.builder()
                .fullName(request.fullName())
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(role)
                .enabled(true)
                .build();
        userRepository.save(user);

        String token = jwtService.generateToken(user, claimsFor(user));
        return ResponseEntity.ok(new AuthResponse(token, user.getUsername(), user.getFullName(), user.getRole().name()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new BadRequestException("User not found"));

        String token = jwtService.generateToken(user, claimsFor(user));
        return ResponseEntity.ok(new AuthResponse(token, user.getUsername(), user.getFullName(), user.getRole().name()));
    }

    private Map<String, Object> claimsFor(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        claims.put("fullName", user.getFullName());
        claims.put("userId", user.getId());
        return claims;
    }
}
