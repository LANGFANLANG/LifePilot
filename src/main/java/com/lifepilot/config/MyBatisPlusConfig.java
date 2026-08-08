package com.lifepilot.config;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * MyBatis-Plus mapper 扫描配置。
 */
@Configuration
@ConditionalOnBean(DataSource.class)
@MapperScan(basePackages = "com.lifepilot.repository", annotationClass = Mapper.class)
public class MyBatisPlusConfig {
}
