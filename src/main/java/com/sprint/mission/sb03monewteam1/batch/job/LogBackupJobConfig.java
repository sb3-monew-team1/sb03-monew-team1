package com.sprint.mission.sb03monewteam1.batch.job;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.sprint.mission.sb03monewteam1.util.S3Util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class LogBackupJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final S3Util s3Util;

    @Value("${aws.s3.bucket:}")
    private String backupBucket;

    @Value("${aws.s3.log-prefix:logs}")
    private String logPrefix;

    @Value("${aws.s3.logDir:/app/logs}")
    private String logDir;

    @Bean
    public Job logBackupJob() {
        return new JobBuilder("logBackupJob", jobRepository)
            .start(logBackupStep())
            .build();
    }

    @Bean
    public Step logBackupStep() {
        return new StepBuilder("logBackupStep", jobRepository)
            .tasklet(logBackupTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Tasklet logBackupTasklet() {
        return (contribution, chunkContext) -> {
            LocalDate targetDate = LocalDate.now().minusDays(1);
            String dateStr = targetDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            Path logPath = Paths.get(logDir);
            if (!Files.exists(logPath)) {
                log.warn("로그 디렉토리가 존재하지 않습니다: {}", logDir);
                return RepeatStatus.FINISHED;
            }
            List<Path> fileList;
            try (Stream<Path> files = Files.list(logPath)) {
                fileList = files.filter(Files::isRegularFile)
                    .collect(Collectors.toList());
            } catch (Exception e) {
                throw new RuntimeException("로그 디렉토리 읽기 실패: " + logDir, e);
            }
            long count = fileList.stream()
                .filter(path -> {
                    String name = path.getFileName().toString();
                    return name.matches("(main|debug)-" + dateStr + "(-\\d+)?\\.log\\.gz");
                })
                .peek(path -> uploadFile(targetDate, path))
                .count();
            log.info("로그 백업 완료 - 백업된 파일 개수: {}", count);
            return RepeatStatus.FINISHED;
        };
    }

    private void uploadFile(LocalDate date, Path path) {
        String subDir = path.getFileName().toString().startsWith("main-") ? "main" : "debug";
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String key = String.format("%s/%s/%s/%s", logPrefix, subDir, dateStr, path.getFileName());
        try (InputStream is = Files.newInputStream(path)) {
            s3Util.upload(backupBucket, key, is, Files.size(path), "application/gzip");
            log.info("로그 백업 성공: {} -> {}", path, key);
        } catch (Exception e) {
            log.error("로그 백업 실패: {}", path, e);
        }
    }
}
