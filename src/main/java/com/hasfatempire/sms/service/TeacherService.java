package com.hasfatempire.sms.service;

import com.hasfatempire.sms.exception.ResourceNotFoundException;
import com.hasfatempire.sms.model.Teacher;
import com.hasfatempire.sms.repository.TeacherRepository;
import com.hasfatempire.sms.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final TenantContext tenantContext;

    public List<Teacher> findAll(Authentication auth) {
        return teacherRepository.findBySchoolId(tenantContext.getCurrentSchoolId(auth));
    }

    public Teacher findById(Long id, Authentication auth) {
        Long schoolId = tenantContext.getCurrentSchoolId(auth);
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found: " + id));
        if (teacher.getSchool() == null || !teacher.getSchool().getId().equals(schoolId)) {
            throw new ResourceNotFoundException("Teacher not found: " + id);
        }
        return teacher;
    }

    public Teacher create(Teacher teacher, Authentication auth) {
        teacher.setSchool(tenantContext.getCurrentSchool(auth));
        return teacherRepository.save(teacher);
    }

    public Teacher update(Long id, Teacher updated, Authentication auth) {
        Teacher existing = findById(id, auth);
        existing.setFirstName(updated.getFirstName());
        existing.setLastName(updated.getLastName());
        existing.setEmail(updated.getEmail());
        existing.setPhone(updated.getPhone());
        existing.setSubjectSpecialty(updated.getSubjectSpecialty());
        existing.setStatus(updated.getStatus());
        return teacherRepository.save(existing);
    }

    public void delete(Long id, Authentication auth) {
        teacherRepository.delete(findById(id, auth));
    }
}
