package com.hasfatempire.sms.service;

import com.hasfatempire.sms.exception.ResourceNotFoundException;
import com.hasfatempire.sms.model.SchoolClass;
import com.hasfatempire.sms.repository.SchoolClassRepository;
import com.hasfatempire.sms.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SchoolClassService {

    private final SchoolClassRepository classRepository;
    private final TenantContext tenantContext;

    public List<SchoolClass> findAll(Authentication auth) {
        return classRepository.findBySchoolId(tenantContext.getCurrentSchoolId(auth));
    }

    public SchoolClass findById(Long id, Authentication auth) {
        Long schoolId = tenantContext.getCurrentSchoolId(auth);
        SchoolClass schoolClass = classRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found: " + id));
        if (schoolClass.getSchool() == null || !schoolClass.getSchool().getId().equals(schoolId)) {
            throw new ResourceNotFoundException("Class not found: " + id);
        }
        return schoolClass;
    }

    public SchoolClass create(SchoolClass schoolClass, Authentication auth) {
        schoolClass.setSchool(tenantContext.getCurrentSchool(auth));
        return classRepository.save(schoolClass);
    }

    public SchoolClass update(Long id, SchoolClass updated, Authentication auth) {
        SchoolClass existing = findById(id, auth);
        existing.setName(updated.getName());
        existing.setSection(updated.getSection());
        existing.setHomeroomTeacher(updated.getHomeroomTeacher());
        existing.setAcademicYear(updated.getAcademicYear());
        return classRepository.save(existing);
    }

    public void delete(Long id, Authentication auth) {
        classRepository.delete(findById(id, auth));
    }
}
