package com.sprint.mission.sb03monewteam1.batch.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.sb03monewteam1.dto.ArticleDto;
import com.sprint.mission.sb03monewteam1.repository.ArticleRepositoryCustom;
import com.sprint.mission.sb03monewteam1.util.S3Util;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.IteratorItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ArticleBackupJobConfig {

    private final ArticleRepositoryCustom articleRepositoryCustom;
    private final ObjectMapper objectMapper;
    private final S3Util s3Util;

    @Value("${aws.s3.bucket:}")
    private String backupBucket;

    @Value("${aws.s3.backup-prefix:articles/}")
    private String backupPrefix;

    @Bean
    public Job articleBackupJob(JobRepository jobRepository, Step articleBackupStep) {
        return new JobBuilder("articleBackupJob", jobRepository)
            .start(articleBackupStep)
            .build();
    }

    @Bean
    public Step articleBackupStep(
        JobRepository jobRepository,
        PlatformTransactionManager transactionManager) {
        return new StepBuilder("articleBackupStep", jobRepository)
            .<ArticleDto, ArticleDto>chunk(100, transactionManager)
            .reader(reader())
            .processor(processor())
            .writer(writer())
            .build();
    }

    @Bean
    public ItemReader<ArticleDto> reader() {
        List<ArticleDto> articles = articleRepositoryCustom.findAllCreatedYesterday();
        return new IteratorItemReader<>(articles);
    }

    @Bean
    public ItemProcessor<ArticleDto, ArticleDto> processor() {
        return articleDto -> articleDto;
    }

    @Bean
    public ItemWriter<ArticleDto> writer() {
        return articles -> {
            String key = backupPrefix + "backup-articles-" +
                LocalDate.now(ZoneId.of("Asia/Seoul")).minusDays(1) + ".json";
            try {
                log.info("백업 파일 업로드 시작: key={}", key);
                String json = objectMapper.writeValueAsString(articles);
                byte[] jsonBytes = json.getBytes(StandardCharsets.UTF_8);

                s3Util.upload(
                    backupBucket,
                    key,
                    new java.io.ByteArrayInputStream(jsonBytes),
                    jsonBytes.length,
                    "application/json"
                );

                log.info("백업 파일 업로드 완료: key={}", key);
            } catch (Exception e) {
                log.error("백업 파일 업로드 중 오류 발생", e);
                throw new RuntimeException(e);
            }
        };
    }
}