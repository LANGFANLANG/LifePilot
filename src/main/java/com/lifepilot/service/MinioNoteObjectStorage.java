package com.lifepilot.service;

import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.Http;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Stores uploaded note files in MinIO.
 */
@Service
public class MinioNoteObjectStorage implements NoteObjectStorage {

    private final MinioClient minioClient;
    private final String bucket;

    /**
     * Creates MinIO note object storage.
     *
     * @param minioClient MinIO client
     * @param bucket bucket name
     */
    public MinioNoteObjectStorage(
            MinioClient minioClient,
            @Value("${lifepilot.minio.bucket:lifepilot-notes}") String bucket
    ) {
        this.minioClient = minioClient;
        this.bucket = bucket;
    }

    @Override
    public void putObject(String objectKey, String contentType, Path file, long size) {
        try {
            ensureBucket();
            try (InputStream input = Files.newInputStream(file)) {
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectKey)
                        .contentType(contentType == null || contentType.isBlank() ? "application/octet-stream" : contentType)
                        .stream(input, size, -1L)
                        .build());
            }
        } catch (Exception ex) {
            throw new IllegalStateException("笔记文件上传到 MinIO 失败", ex);
        }
    }

    @Override
    public String temporaryUrl(String objectKey, String downloadName) {
        try {
            ensureBucket();
            GetPresignedObjectUrlArgs.Builder builder = GetPresignedObjectUrlArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .method(Http.Method.GET)
                    .expiry(60 * 10);
            if (downloadName != null && !downloadName.isBlank()) {
                String encoded = URLEncoder.encode(downloadName, StandardCharsets.UTF_8).replace("+", "%20");
                builder.extraQueryParams(Map.of("response-content-disposition", "attachment; filename*=UTF-8''" + encoded));
            }
            return minioClient.getPresignedObjectUrl(builder.build());
        } catch (Exception ex) {
            throw new IllegalStateException("笔记文件访问链接生成失败", ex);
        }
    }

    @Override
    public void deleteObject(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) return;
        try {
            ensureBucket();
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .build());
        } catch (Exception ex) {
            throw new IllegalStateException("笔记文件删除失败", ex);
        }
    }

    private void ensureBucket() throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(bucket)
                .build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucket)
                    .build());
        }
    }
}
