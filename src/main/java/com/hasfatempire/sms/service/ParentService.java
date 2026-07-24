package com.hasfatempire.sms.service;

import com.hasfatempire.sms.exception.ResourceNotFoundException;
import com.hasfatempire.sms.model.ParentGuardian;
import com.hasfatempire.sms.repository.ParentGuardianRepository;
import com.hasfatempire.sms.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParentService {

    private final ParentGuardianRepository parentRepository;
    private final StudentRepository studentRepository;

    public List<ParentGuardian> findAll() { return parentRepository.findAll(); }

    public ParentGuardian findById(Long id) {
        return parentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parent/Guardian not found: " + id));
    }

    public ParentGuardian create(ParentGuardian parent) { return parentRepository.save(parent); }

    public ParentGuardian update(Long id, ParentGuardian updated) {
        ParentGuardian existing = findById(id);
        existing.setFirstName(updated.getFirstName());
        existing.setLastName(updated.getLastName());
        existing.setPhone(updated.getPhone());
        existing.setEmail(updated.getEmail());
        existing.setOccupation(updated.getOccupation());
        existing.setAddress(updated.getAddress());
        existing.setRelationship(updated.getRelationship());
        return parentRepository.save(existing);
    }

    public void delete(Long id) { parentRepository.delete(findById(id)); }

    public Object children(Long id) { return studentRepository.findByParentGuardianId(id); }
}
