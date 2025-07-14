package com.sprint.mission.sb03monewteam1.util;

import com.sprint.mission.sb03monewteam1.exception.util.S3DownloadException;
import com.sprint.mission.sb03monewteam1.exception.util.S3UploadException;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public void upload(String bucketName, String key, InputStream inputStream, long length,
        String contentType) {
        log.info("S3 파일 업로드 시작: bucket={}, key={}, length={}, contentType={}", bucketName, key,
            length, contentType);

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, length));

            log.info("S3 파일 업로드 성공: bucket={}, key={}", bucketName, key);
        } catch (Exception e) {
            log.error("S3 파일 업로드 실패: bucket={}, key={}", bucketName, key, e);
            throw new S3UploadException("S3 파일 업로드 실패: " + e.getMessage());
        }
    }

    public byte[] download(String bucketName, String key) {
        log.info("S3 파일 다운로드 시작: bucket={}, key={}", bucketName, key);

        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(
                getObjectRequest);

            log.info("S3 파일 다운로드 완료: bucket={}, key={}", bucketName, key);
            return objectBytes.asByteArray();
        } catch (Exception e) {
            log.error("S3 파일 다운로드 실패: bucket={}, key={}", bucketName, key, e);
            throw new S3DownloadException("S3 파일 다운로드 실패: " + e.getMessage());
        }
    }
}
