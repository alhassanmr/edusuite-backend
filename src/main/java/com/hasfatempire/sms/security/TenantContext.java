package com.hasfatempire.sms.security;

import com.hasfatempire.sms.exception.BadRequestException;
import com.hasfatempire.sms.model.User;
import com.hasfatempire.sms.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Helper to extract current tenant (school) context from authentication
 */
@Component
public class TenantContext {

    private final UserRepository userRepository;

    public TenantContext(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Get the current school ID from authenticated user
     */
    public Long getCurrentSchoolId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BadRequestException("User not authenticated");
        }

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (user.getSchool() == null) {
            throw new BadRequestException("User has no associated school");
        }

        return user.getSchool().getId();
    }

    /**
     * Get the current school object
     */
    public com.hasfatempire.sms.model.School getCurrentSchool(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BadRequestException("User not authenticated");
        }

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (user.getSchool() == null) {
            throw new BadRequestException("User has no associated school");
        }

        return user.getSchool();
    }
}
