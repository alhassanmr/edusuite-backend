package com.hasfatempire.sms.dto;

import com.hasfatempire.sms.model.Attendance;

import java.time.LocalDate;
import java.util.List;

public record AttendanceMarkRequest(
        Long schoolClassId,
        LocalDate date,
        List<Entry> entries
) {
    public record Entry(Long studentId, Attendance.AttendanceStatus status, String remarks) {}
}
