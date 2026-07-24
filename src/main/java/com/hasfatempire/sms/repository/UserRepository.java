package com.hasfatempire.sms.repository;

import com.hasfatempire.sms.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByUsernameAndSchoolId(String username, Long schoolId);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<User> findBySchoolId(Long schoolId);
}
