package com.hasfatempire.sms.repository;

import com.hasfatempire.sms.model.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    List<Notice> findAllByOrderByPostedAtDesc();
    List<Notice> findByAudienceInOrderByPostedAtDesc(List<Notice.Audience> audiences);
}
