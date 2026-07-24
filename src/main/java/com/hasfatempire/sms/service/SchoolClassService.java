package com.hasfatempire.sms.service;

import com.hasfatempire.sms.exception.ResourceNotFoundException;
import com.hasfatempire.sms.model.SchoolClass;
import com.hasfatempire.sms.repository.SchoolClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SchoolClassService {

    private final SchoolClassRepository classRepository;

    public List<SchoolClass> findAll() { return classRepository.findAll(); }

    public SchoolClass findById(Long id) {
        return classRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found: " + id));
    }

    public SchoolClass create(SchoolClass schoolClass) { return classRepository.save(schoolClass); }

    public SchoolClass update(Long id, SchoolClass updated) {
        SchoolClass existing = findById(id);
        existing.setName(updated.getName());
        existing.setSection(updated.getSection());
        existing.setHomeroomTeacher(updated.getHomeroomTeacher());
        existing.setAcademicYear(updated.getAcademicYear());
        return classRepository.save(existing);
    }

    public void delete(Long id) { classRepository.delete(findById(id)); }
}
