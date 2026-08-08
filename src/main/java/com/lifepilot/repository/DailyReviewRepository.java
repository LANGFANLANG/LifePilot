package com.lifepilot.repository;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lifepilot.domain.DailyReview;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.Optional;

/**
 * 每日复盘持久化入口。
 */
@Mapper
public interface DailyReviewRepository extends MyBatisRepository<DailyReview> {

    default Optional<DailyReview> findByReviewDate(LocalDate reviewDate) {
        return Optional.ofNullable(selectOne(Wrappers.lambdaQuery(DailyReview.class)
                .eq(DailyReview::getReviewDate, reviewDate)));
    }

    default DailyReview save(DailyReview review) {
        return save(review, DailyReview::getId);
    }
}
