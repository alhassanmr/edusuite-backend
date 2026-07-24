package com.hasfatempire.sms.service;

import com.hasfatempire.sms.exception.ResourceNotFoundException;
import com.hasfatempire.sms.model.Student;
import com.hasfatempire.sms.repository.StudentRepository;
import com.hasfatempire.sms.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final TenantContext tenantContext;

    public List<Student> findAll(Authentication auth) {
        Long schoolId = tenantContext.getCurrentSchoolId(auth);
        return studentRepository.findBySchoolId(schoolId);
    }

    public Student findById(Long id, Authentication auth) {
        Long schoolId = tenantContext.getCurrentSchoolId(auth);
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + id));
        if (!student.getSchool().getId().equals(schoolId)) {
            throw new ResourceNotFoundException("Student not found: " + id);
        }
        return student;
    }

    public List<Student> findByClass(Long classId) {
        return studentRepository.findBySchoolClassId(classId);
    }

    public List<Student> search(String query) {
        return studentRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(query, query);
    }

    public Student create(Student student) {
        return studentRepository.save(student);
    }

    public Student update(Long id, Student updated) {
        Student existing = findById(id);
        existing.setFirstName(updated.getFirstName());
        existing.setLastName(updated.getLastName());
        existing.setDateOfBirth(updated.getDateOfBirth());
        existing.setGender(updated.getGender());
        existing.setAddress(updated.getAddress());
        existing.setStatus(updated.getStatus());
        existing.setSchoolClass(updated.getSchoolClass());
        existing.setParentGuardian(updated.getParentGuardian());
        if (updated.getPhotoUrl() != null) existing.setPhotoUrl(updated.getPhotoUrl());
        return studentRepository.save(existing);
    }

    public void delete(Long id) {
        studentRepository.delete(findById(id));
    }
}
