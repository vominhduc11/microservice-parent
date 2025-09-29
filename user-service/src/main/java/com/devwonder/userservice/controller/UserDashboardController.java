package com.devwonder.userservice.controller;

import com.devwonder.userservice.service.UserDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user-service/dashboard")
@RequiredArgsConstructor
@Slf4j
public class UserDashboardController {

    private final UserDashboardService dashboardService;

    @GetMapping("/dealer-counts")
    public Map<String, Long> getDealerCounts(@RequestHeader("X-API-Key") String apiKey) {
        log.debug("Getting dealer counts");
        return dashboardService.getDealerCounts();
    }

    @GetMapping("/monthly-dealer-growth")
    public Map<String, Double> getMonthlyDealerGrowth(@RequestHeader("X-API-Key") String apiKey) {
        log.debug("Getting monthly dealer growth");
        return dashboardService.getMonthlyDealerGrowth();
    }
}