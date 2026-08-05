package com.lifepilot.controller;

import com.lifepilot.api.Result;
import com.lifepilot.service.ReminderService;
import com.lifepilot.service.dto.ReminderDeliveryView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 提供站内提醒记录接口。
 */
@RestController
@RequestMapping("/api/reminders")
public class ReminderController {

    private final ReminderService reminderService;

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @GetMapping("/recent")
    public Result<List<ReminderDeliveryView>> recent() {
        return Result.success(reminderService.listRecent());
    }
}
