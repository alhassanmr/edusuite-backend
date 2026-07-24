package com.hasfatempire.sms.service;

import com.hasfatempire.sms.dto.DashboardStatsResponse;
import com.hasfatempire.sms.repository.*;
import com.hasfatempire.sms.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
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
    private final TenantContext tenantContext;

    public DashboardStatsResponse getStats(Authentication auth) {
        Long schoolId = tenantContext.getCurrentSchoolId(auth);
        return new DashboardStatsResponse(
                studentRepository.findBySchoolId(schoolId).size(),
                teacherRepository.findBySchoolId(schoolId).size(),
                parentRepository.findBySchoolId(schoolId).size(),
                classRepository.findBySchoolId(schoolId).size(),
                attendanceService.todayAttendancePercent(),
                feeService.totalCollected(schoolId),
                feeService.totalOutstanding(schoolId),
                noticeRepository.countBySchoolId(schoolId)
        );
    }
}
