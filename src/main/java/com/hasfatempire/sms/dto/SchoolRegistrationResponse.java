package com.hasfatempire.sms.dto;

public record SchoolRegistrationResponse(
        Long schoolId,
        String schoolName,
        String schoolSlug,
        String adminUsername,
        String token,
        String message
) {}
