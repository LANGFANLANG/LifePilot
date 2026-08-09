package com.lifepilot.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO client configuration for uploaded note files.
 */
@Configuration
public class MinioConfig {

    /**
     * Creates MinIO client.
     *
     * @param endpoint MinIO endpoint
     * @param accessKey MinIO access key
     * @param secretKey MinIO secret key
     * @return MinIO client
     */
    @Bean
    public MinioClient minioClient(
            @Value("${lifepilot.minio.endpoint:http://localhost:9000}") String endpoint,
            @Value("${lifepilot.minio.access-key:lifepilot}") String accessKey,
            @Value("${lifepilot.minio.secret-key:lifepilot-secret}") String secretKey
    ) {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}
