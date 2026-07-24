package com.hasfatempire.sms.dto;

public record SchoolSettingsResponse(
        Long schoolId,
        String name,
        String slug,
        String contactEmail,
        String phone,
        String address,
        String city,
        String country,
        String logoUrl,
        String theme,
        String status
) {}
