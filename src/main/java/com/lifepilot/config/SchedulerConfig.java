package com.lifepilot.config;

import com.lifepilot.service.ReminderService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.OffsetDateTime;

/**
 * 配置并执行后台定时任务。
 */
@Component
@EnableScheduling
public class SchedulerConfig {

    private final ReminderService reminderService;
    private final Clock clock;

    public SchedulerConfig(ReminderService reminderService, Clock clock) {
        this.reminderService = reminderService;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${lifepilot.reminders.scan-delay-ms:60000}")
    public void scanDueReminders() {
        reminderService.deliverDueReminders(OffsetDateTime.now(clock));
    }
}
