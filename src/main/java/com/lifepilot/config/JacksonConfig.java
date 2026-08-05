package com.lifepilot.config;

import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.OffsetDateTime;

/**
 * Jackson 时间类型反序列化配置。
 */
@Configuration
public class JacksonConfig {

    /**
     * 注册宽松的 {@link OffsetDateTime} 反序列化器。
     *
     * @return Jackson 对象构建器自定义器
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer lenientOffsetDateTimeCustomizer() {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(OffsetDateTime.class, new LenientOffsetDateTimeDeserializer());
        return builder -> builder.modules(module);
    }
}
