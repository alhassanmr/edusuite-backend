package com.hasfatempire.sms.service;

import com.hasfatempire.sms.exception.ResourceNotFoundException;
import com.hasfatempire.sms.model.Subject;
import com.hasfatempire.sms.repository.SubjectRepository;
import com.hasfatempire.sms.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final TenantContext tenantContext;

    public List<Subject> findAll(Authentication auth) {
        return subjectRepository.findBySchoolId(tenantContext.getCurrentSchoolId(auth));
    }

    public Subject create(Subject subject, Authentication auth) {
        subject.setSchool(tenantContext.getCurrentSchool(auth));
        return subjectRepository.save(subject);
    }

    public void delete(Long id, Authentication auth) {
        Long schoolId = tenantContext.getCurrentSchoolId(auth);
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found: " + id));
        if (subject.getSchool() == null || !subject.getSchool().getId().equals(schoolId)) {
            throw new ResourceNotFoundException("Subject not found: " + id);
        }
        subjectRepository.delete(subject);
    }
}
