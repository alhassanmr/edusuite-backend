package com.hasfatempire.sms.repository;

import com.hasfatempire.sms.model.School;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SchoolRepository extends JpaRepository<School, Long> {
    Optional<School> findBySlug(String slug);
    boolean existsBySlug(String slug);
    Optional<School> findByContactEmail(String contactEmail);
}
