package com.hasfatempire.sms.service;

import com.hasfatempire.sms.exception.ResourceNotFoundException;
import com.hasfatempire.sms.model.ParentGuardian;
import com.hasfatempire.sms.repository.ParentGuardianRepository;
import com.hasfatempire.sms.repository.StudentRepository;
import com.hasfatempire.sms.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParentService {

    private final ParentGuardianRepository parentRepository;
    private final StudentRepository studentRepository;
    private final TenantContext tenantContext;

    public List<ParentGuardian> findAll(Authentication auth) {
        return parentRepository.findBySchoolId(tenantContext.getCurrentSchoolId(auth));
    }

    public ParentGuardian findById(Long id, Authentication auth) {
        Long schoolId = tenantContext.getCurrentSchoolId(auth);
        ParentGuardian parent = parentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parent/Guardian not found: " + id));
        if (parent.getSchool() == null || !parent.getSchool().getId().equals(schoolId)) {
            throw new ResourceNotFoundException("Parent/Guardian not found: " + id);
        }
        return parent;
    }

    public ParentGuardian create(ParentGuardian parent, Authentication auth) {
        parent.setSchool(tenantContext.getCurrentSchool(auth));
        return parentRepository.save(parent);
    }

    public ParentGuardian update(Long id, ParentGuardian updated, Authentication auth) {
        ParentGuardian existing = findById(id, auth);
        existing.setFirstName(updated.getFirstName());
        existing.setLastName(updated.getLastName());
        existing.setPhone(updated.getPhone());
        existing.setEmail(updated.getEmail());
        existing.setOccupation(updated.getOccupation());
        existing.setAddress(updated.getAddress());
        existing.setRelationship(updated.getRelationship());
        return parentRepository.save(existing);
    }

    public void delete(Long id, Authentication auth) {
        parentRepository.delete(findById(id, auth));
    }

    public Object children(Long id, Authentication auth) {
        findById(id, auth); // ownership check
        return studentRepository.findByParentGuardianId(id);
    }
}
