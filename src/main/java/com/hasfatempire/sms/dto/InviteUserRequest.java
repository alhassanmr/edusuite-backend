package com.hasfatempire.sms.dto;

import jakarta.validation.constraints.*;

/**
 * Admin invites a parent, student, or teacher — creates a linked login account
 * and sends credentials via SMS and/or email.
 */
public record InviteUserRequest(
        @NotBlank String fullName,
        @NotBlank @Size(min = 3) String username,
        @Email @NotBlank String email,
        String phone,
        @NotBlank String role,       // PARENT | STUDENT | TEACHER
        Long linkedRecordId,          // parentId, studentId, or teacherId to link
        @NotBlank @Size(min = 6) String temporaryPassword,
        boolean sendSms,
        boolean sendEmail
) {}
