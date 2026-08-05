package com.lifepilot.controller;

import com.lifepilot.api.Result;
import com.lifepilot.service.PlanningService;
import com.lifepilot.service.dto.TodayPlanView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 提供今日执行计划接口。
 */
@RestController
@RequestMapping("/api/planning")
public class PlanningController {

    private final PlanningService planningService;

    public PlanningController(PlanningService planningService) {
        this.planningService = planningService;
    }

    @GetMapping("/today")
    public Result<TodayPlanView> today() {
        return Result.success(planningService.today());
    }
}
