package com.hasfatempire.sms.service;

import com.hasfatempire.sms.exception.ResourceNotFoundException;
import com.hasfatempire.sms.model.TimetableEntry;
import com.hasfatempire.sms.repository.TimetableEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TimetableService {

    private final TimetableEntryRepository timetableRepository;

    public List<TimetableEntry> byClass(Long classId) { return timetableRepository.findBySchoolClassId(classId); }

    public List<TimetableEntry> byTeacher(Long teacherId) { return timetableRepository.findByTeacherId(teacherId); }

    public TimetableEntry create(TimetableEntry entry) { return timetableRepository.save(entry); }

    public void delete(Long id) {
        if (!timetableRepository.existsById(id)) throw new ResourceNotFoundException("Timetable entry not found: " + id);
        timetableRepository.deleteById(id);
    }
}
