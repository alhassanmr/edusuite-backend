package com.hasfatempire.sms.dto;

import jakarta.validation.constraints.*;

public record SchoolRegistrationRequest(
        @NotBlank String schoolName,
        @NotBlank String slug, // unique school identifier
        @Email @NotBlank String adminEmail,
        @NotBlank @Size(min = 3) String adminUsername,
        @NotBlank @Size(min = 6) String adminPassword,
        String adminFullName,
        @NotBlank String schoolContactEmail,
        String schoolPhone,
        String schoolAddress,
        String schoolCity,
        String schoolCountry
) {}
