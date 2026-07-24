package com.hasfatempire.sms.repository;

import com.hasfatempire.sms.model.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    List<Notice> findBySchoolIdOrderByPostedAtDesc(Long schoolId);
    List<Notice> findBySchoolIdAndAudienceInOrderByPostedAtDesc(Long schoolId, List<Notice.Audience> audiences);
    long countBySchoolId(Long schoolId);
}
