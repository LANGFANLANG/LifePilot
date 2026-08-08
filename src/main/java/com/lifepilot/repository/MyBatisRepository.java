package com.lifepilot.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

interface MyBatisRepository<T> extends BaseMapper<T> {

    default T save(T entity, Function<T, UUID> idExtractor) {
        UUID id = idExtractor.apply(entity);
        int updated = id == null ? 0 : updateById(entity);
        if (updated == 0) {
            insert(entity);
        }
        return entity;
    }

    default List<T> findAll() {
        return selectList(null);
    }
}
