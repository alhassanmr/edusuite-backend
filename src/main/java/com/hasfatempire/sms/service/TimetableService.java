package com.hasfatempire.sms.service;

import com.hasfatempire.sms.exception.ResourceNotFoundException;
import com.hasfatempire.sms.model.TimetableEntry;
import com.hasfatempire.sms.repository.TimetableEntryRepository;
import com.hasfatempire.sms.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TimetableService {

    private final TimetableEntryRepository timetableRepository;
    private final TenantContext tenantContext;

    public List<TimetableEntry> findAll(Authentication auth) {
        return timetableRepository.findBySchoolId(tenantContext.getCurrentSchoolId(auth));
    }

    public List<TimetableEntry> byClass(Long classId) {
        return timetableRepository.findBySchoolClassId(classId);
    }

    public List<TimetableEntry> byTeacher(Long teacherId) {
        return timetableRepository.findByTeacherId(teacherId);
    }

    public TimetableEntry create(TimetableEntry entry, Authentication auth) {
        entry.setSchool(tenantContext.getCurrentSchool(auth));
        return timetableRepository.save(entry);
    }

    public void delete(Long id, Authentication auth) {
        Long schoolId = tenantContext.getCurrentSchoolId(auth);
        TimetableEntry entry = timetableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Timetable entry not found: " + id));
        if (entry.getSchool() == null || !entry.getSchool().getId().equals(schoolId)) {
            throw new ResourceNotFoundException("Timetable entry not found: " + id);
        }
        timetableRepository.delete(entry);
    }
}
