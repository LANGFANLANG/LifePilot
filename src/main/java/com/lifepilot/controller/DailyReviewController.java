package com.lifepilot.controller;

import com.lifepilot.api.Result;
import com.lifepilot.controller.dto.SaveDailyReviewRequest;
import com.lifepilot.service.DailyReviewService;
import com.lifepilot.service.dto.DailyReviewView;
import com.lifepilot.service.dto.SaveDailyReviewCommand;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * 提供每日复盘草稿、读取和保存接口。
 */
@RestController
@RequestMapping("/api/reviews/daily")
public class DailyReviewController {

    private final DailyReviewService dailyReviewService;

    public DailyReviewController(DailyReviewService dailyReviewService) {
        this.dailyReviewService = dailyReviewService;
    }

    @PostMapping("/{date}/draft")
    public Result<DailyReviewView> draft(@PathVariable LocalDate date) {
        return Result.success(dailyReviewService.draft(date));
    }

    @GetMapping("/{date}")
    public Result<DailyReviewView> get(@PathVariable LocalDate date) {
        return Result.success(dailyReviewService.get(date));
    }

    @PutMapping("/{date}")
    public Result<DailyReviewView> save(
            @PathVariable LocalDate date,
            @RequestBody SaveDailyReviewRequest request
    ) {
        return Result.success(dailyReviewService.save(date, new SaveDailyReviewCommand(
                request.completedSummary(),
                request.unfinishedSummary(),
                request.newTasksSummary(),
                request.reflection(),
                request.tomorrowPlan()
        )));
    }
}
