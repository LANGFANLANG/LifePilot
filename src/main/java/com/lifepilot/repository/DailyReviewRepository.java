package com.lifepilot.repository;

import com.lifepilot.domain.DailyReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * 每日复盘持久化入口。
 */
public interface DailyReviewRepository extends JpaRepository<DailyReview, UUID> {

    Optional<DailyReview> findByReviewDate(LocalDate reviewDate);
}
