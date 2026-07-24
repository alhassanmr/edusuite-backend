package com.hasfatempire.sms.controller;

import com.hasfatempire.sms.dto.DashboardStatsResponse;
import com.hasfatempire.sms.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public DashboardStatsResponse stats(Authentication auth) { return dashboardService.getStats(auth); }
}
