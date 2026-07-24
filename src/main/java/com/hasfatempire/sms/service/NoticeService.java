package com.hasfatempire.sms.service;

import com.hasfatempire.sms.exception.ResourceNotFoundException;
import com.hasfatempire.sms.model.Notice;
import com.hasfatempire.sms.repository.NoticeRepository;
import com.hasfatempire.sms.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final TenantContext tenantContext;

    public List<Notice> findAll(Authentication auth) {
        return noticeRepository.findBySchoolIdOrderByPostedAtDesc(tenantContext.getCurrentSchoolId(auth));
    }

    public List<Notice> findForAudience(Long schoolId, List<Notice.Audience> audiences) {
        return noticeRepository.findBySchoolIdAndAudienceInOrderByPostedAtDesc(schoolId, audiences);
    }

    public Notice findById(Long id, Authentication auth) {
        Long schoolId = tenantContext.getCurrentSchoolId(auth);
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notice not found: " + id));
        if (notice.getSchool() == null || !notice.getSchool().getId().equals(schoolId)) {
            throw new ResourceNotFoundException("Notice not found: " + id);
        }
        return notice;
    }

    public Notice create(Notice notice, Authentication auth) {
        notice.setSchool(tenantContext.getCurrentSchool(auth));
        notice.setPostedBy(auth.getName());
        return noticeRepository.save(notice);
    }

    public void delete(Long id, Authentication auth) {
        noticeRepository.delete(findById(id, auth));
    }
}
