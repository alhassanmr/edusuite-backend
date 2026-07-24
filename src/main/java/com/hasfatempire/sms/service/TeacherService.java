package com.hasfatempire.sms.service;

import com.hasfatempire.sms.exception.ResourceNotFoundException;
import com.hasfatempire.sms.model.Teacher;
import com.hasfatempire.sms.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;

    public List<Teacher> findAll() { return teacherRepository.findAll(); }

    public Teacher findById(Long id) {
        return teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found: " + id));
    }

    public Teacher create(Teacher teacher) { return teacherRepository.save(teacher); }

    public Teacher update(Long id, Teacher updated) {
        Teacher existing = findById(id);
        existing.setFirstName(updated.getFirstName());
        existing.setLastName(updated.getLastName());
        existing.setEmail(updated.getEmail());
        existing.setPhone(updated.getPhone());
        existing.setSubjectSpecialty(updated.getSubjectSpecialty());
        existing.setStatus(updated.getStatus());
        return teacherRepository.save(existing);
    }

    public void delete(Long id) { teacherRepository.delete(findById(id)); }
}
