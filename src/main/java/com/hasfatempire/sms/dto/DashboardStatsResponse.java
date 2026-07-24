package com.hasfatempire.sms.dto;

import java.math.BigDecimal;

public record DashboardStatsResponse(
        long totalStudents,
        long totalTeachers,
        long totalParents,
        long totalClasses,
        double todayAttendancePercent,
        BigDecimal feesCollectedThisTerm,
        BigDecimal feesOutstanding,
        long unreadNoticesCount
) {}
