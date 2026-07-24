package com.hasfatempire.sms.controller;

import com.hasfatempire.sms.dto.AttendanceMarkRequest;
import com.hasfatempire.sms.model.Attendance;
import com.hasfatempire.sms.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/mark")
    public List<Attendance> mark(@RequestBody AttendanceMarkRequest request, Authentication authentication) {
        return attendanceService.markBulk(request, authentication.getName());
    }

    @GetMapping("/class/{classId}")
    public List<Attendance> byClass(@PathVariable Long classId,
                                     @RequestParam(required = false)
                                     @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
                                     LocalDate date) {
        return attendanceService.byClassAndDate(classId, date != null ? date : LocalDate.now());
    }

    @GetMapping("/student/{studentId}")
    public List<Attendance> byStudent(@PathVariable Long studentId) {
        return attendanceService.byStudent(studentId);
    }
}
