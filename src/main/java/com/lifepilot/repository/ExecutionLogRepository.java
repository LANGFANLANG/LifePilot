package com.lifepilot.repository;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lifepilot.domain.ExecutionLog;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 执行日志持久化访问入口。
 */
@Mapper
public interface ExecutionLogRepository extends MyBatisRepository<ExecutionLog> {

    /**
     * 按创建时间倒序获取全部执行日志。
     *
     * @return 已排序的执行日志列表
     */
    default List<ExecutionLog> findAllByOrderByCreatedAtDesc() {
        return selectList(Wrappers.lambdaQuery(ExecutionLog.class)
                .orderByDesc(ExecutionLog::getCreatedAt));
    }

    default ExecutionLog save(ExecutionLog log) {
        return save(log, ExecutionLog::getId);
    }
}
