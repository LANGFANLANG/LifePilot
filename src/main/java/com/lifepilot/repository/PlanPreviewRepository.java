package com.lifepilot.repository;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lifepilot.domain.PlanPreview;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;
import java.util.UUID;

/**
 * AI 计划草案的持久化入口。
 */
@Mapper
public interface PlanPreviewRepository extends MyBatisRepository<PlanPreview> {

    default PlanPreview save(PlanPreview preview) {
        return save(preview, PlanPreview::getId);
    }

    default Optional<PlanPreview> findById(UUID id) {
        return Optional.ofNullable(selectOne(Wrappers.lambdaQuery(PlanPreview.class)
                .eq(PlanPreview::getId, id)));
    }
}
