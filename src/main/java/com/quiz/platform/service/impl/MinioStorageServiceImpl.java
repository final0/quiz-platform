package com.quiz.platform.service.impl;

import com.quiz.platform.common.BizException;
import com.quiz.platform.service.MinioStorageService;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.BucketExistsArgs;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioStorageServiceImpl implements MinioStorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    @PostConstruct
    public void ensureBucket() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("MinIO桶不存在，已自动创建: {}", bucket);
            }
        } catch (Exception e) {
            log.warn("MinIO桶检查/创建失败，若MinIO未启动可先忽略: {}", e.getMessage());
        }
    }

    @Override
    public String upload(MultipartFile file, String objectPrefix) {
        String ext = getExtension(file.getOriginalFilename());
        String objectName = objectPrefix + "/" + LocalDate.now() + "/" + UUID.randomUUID() + ext;
        try (InputStream is = file.getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .stream(is, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            return objectName;
        } catch (Exception e) {
            throw new BizException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public InputStream download(String objectPath) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectPath)
                    .build());
        } catch (Exception e) {
            throw new BizException("文件下载失败: " + e.getMessage());
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.'));
    }
}
