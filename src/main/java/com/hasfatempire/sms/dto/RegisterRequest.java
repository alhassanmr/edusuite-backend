package com.hasfatempire.sms.dto;

import com.hasfatempire.sms.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank String fullName,
        @NotBlank String username,
        @Email @NotBlank String email,
        @NotBlank @Size(min = 6, message = "Password must be at least 6 characters") String password,
        @NotBlank String role // ADMIN, TEACHER, PARENT, STUDENT
) {}
