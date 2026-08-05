package com.lifepilot.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.lifepilot.util.TimeParser;

import java.io.IOException;
import java.time.OffsetDateTime;

/**
 * 宽松的 {@link OffsetDateTime} 反序列化器。
 *
 * <p>接受带时区偏移的 ISO-8601 字符串，也接受无偏移的本地时间或纯日期字符串。</p>
 */
public class LenientOffsetDateTimeDeserializer extends JsonDeserializer<OffsetDateTime> {

    @Override
    public OffsetDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        return TimeParser.parseOffsetDateTime(parser.getValueAsString());
    }
}
