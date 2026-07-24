package com.hasfatempire.sms.repository;

import com.hasfatempire.sms.model.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
    List<NotificationLog> findBySchoolIdOrderByCreatedAtDesc(Long schoolId);
}
