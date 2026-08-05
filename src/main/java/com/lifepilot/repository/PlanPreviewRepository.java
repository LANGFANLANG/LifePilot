package com.lifepilot.repository;

import com.lifepilot.domain.PlanPreview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * AI 计划草案的持久化入口。
 */
public interface PlanPreviewRepository extends JpaRepository<PlanPreview, UUID> {
}
