package com.lifepilot.repository;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lifepilot.domain.PlanPreviewTask;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.UUID;

/**
 * AI 计划草案任务的持久化入口。
 */
@Mapper
public interface PlanPreviewTaskRepository extends MyBatisRepository<PlanPreviewTask> {

    default List<PlanPreviewTask> findByPlanPreviewIdOrderBySortOrderAsc(UUID planPreviewId) {
        return selectList(Wrappers.lambdaQuery(PlanPreviewTask.class)
                .eq(PlanPreviewTask::getPlanPreviewId, planPreviewId)
                .orderByAsc(PlanPreviewTask::getSortOrder));
    }

    default void deleteByPlanPreviewId(UUID planPreviewId) {
        delete(Wrappers.lambdaQuery(PlanPreviewTask.class)
                .eq(PlanPreviewTask::getPlanPreviewId, planPreviewId));
    }

    default PlanPreviewTask save(PlanPreviewTask task) {
        return save(task, PlanPreviewTask::getId);
    }
}
