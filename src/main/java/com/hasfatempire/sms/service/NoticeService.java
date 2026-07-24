package com.hasfatempire.sms.service;

import com.hasfatempire.sms.exception.ResourceNotFoundException;
import com.hasfatempire.sms.model.Notice;
import com.hasfatempire.sms.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;

    public List<Notice> findAll() { return noticeRepository.findAllByOrderByPostedAtDesc(); }

    public List<Notice> findForAudience(List<Notice.Audience> audiences) {
        return noticeRepository.findByAudienceInOrderByPostedAtDesc(audiences);
    }

    public Notice findById(Long id) {
        return noticeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notice not found: " + id));
    }

    public Notice create(Notice notice, String postedBy) {
        notice.setPostedBy(postedBy);
        return noticeRepository.save(notice);
    }

    public void delete(Long id) { noticeRepository.delete(findById(id)); }
}
