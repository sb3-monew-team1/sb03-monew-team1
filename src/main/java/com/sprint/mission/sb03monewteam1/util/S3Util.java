package com.sprint.mission.sb03monewteam1.util;

import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3Util {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public void upload(String key, InputStream inputStream, long length, String contentType) {
        log.info("S3 파일 업로드 시작: key={}, length={}, contentType={}", key, length, contentType);

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, length));

            log.info("S3 파일 업로드 성공: key={}", key);
        } catch (Exception e) {
            log.error("S3 파일 업로드 실패: key={}, message={}", key, e.getMessage(), e);
            throw new RuntimeException("S3 파일 업로드 실패", e);
        }
    }

    public byte[] download(String key) {
        log.info("S3 파일 다운로드 시작: key={}", key);

        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(
                getObjectRequest);

            log.info("S3 파일 다운로드 완료: key={}", key);
            return objectBytes.asByteArray();
        } catch (Exception e) {
            log.error("S3 파일 다운로드 실패: key={}, message={}", key, e.getMessage(), e);
            throw new RuntimeException("S3 파일 다운로드 실패", e);
        }
    }
}
