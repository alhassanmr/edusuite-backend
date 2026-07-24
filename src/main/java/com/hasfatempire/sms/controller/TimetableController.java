package com.hasfatempire.sms.controller;

import com.hasfatempire.sms.model.TimetableEntry;
import com.hasfatempire.sms.service.TimetableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/timetable")
@RequiredArgsConstructor
public class TimetableController {

    private final TimetableService timetableService;

    @GetMapping("/class/{classId}")
    public List<TimetableEntry> byClass(@PathVariable Long classId) { return timetableService.byClass(classId); }

    @GetMapping("/teacher/{teacherId}")
    public List<TimetableEntry> byTeacher(@PathVariable Long teacherId) { return timetableService.byTeacher(teacherId); }

    @PostMapping
    public ResponseEntity<TimetableEntry> create(@Valid @RequestBody TimetableEntry entry) {
        return ResponseEntity.ok(timetableService.create(entry));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        timetableService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
