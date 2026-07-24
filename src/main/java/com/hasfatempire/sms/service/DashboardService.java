package com.hasfatempire.sms.service;

import com.hasfatempire.sms.dto.DashboardStatsResponse;
import com.hasfatempire.sms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final ParentGuardianRepository parentRepository;
    private final SchoolClassRepository classRepository;
    private final NoticeRepository noticeRepository;
    private final AttendanceService attendanceService;
    private final FeeService feeService;

    public DashboardStatsResponse getStats() {
        return new DashboardStatsResponse(
                studentRepository.count(),
                teacherRepository.count(),
                parentRepository.count(),
                classRepository.count(),
                attendanceService.todayAttendancePercent(),
                feeService.totalCollected(),
                feeService.totalOutstanding(),
                noticeRepository.count()
        );
    }
}
