package com.sprint.mission.sb03monewteam1.batch.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.sb03monewteam1.config.metric.MonewMetrics;
import com.sprint.mission.sb03monewteam1.dto.ArticleDto;
import com.sprint.mission.sb03monewteam1.event.listener.JobCompletionMetricsListener;
import com.sprint.mission.sb03monewteam1.repository.jpa.article.ArticleRepositoryCustom;
import com.sprint.mission.sb03monewteam1.util.S3Util;
import io.micrometer.core.instrument.Timer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
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
    private final MonewMetrics monewMetrics;

    @Value("${aws.s3.bucket:}")
    private String backupBucket;

    @Value("${aws.s3.backup-prefix:articles}")
    private String backupPrefix;

    @Bean
    public JobExecutionListener articleBackupJobExecutionListener() {
        return new JobCompletionMetricsListener("articleBackupJob", monewMetrics);
    }

    @Bean
    public Job articleBackupJob(JobRepository jobRepository, Step articleBackupStep,
        JobExecutionListener articleBackupJobExecutionListener) {
        return new JobBuilder("articleBackupJob", jobRepository)
            .start(articleBackupStep)
            .listener(articleBackupJobExecutionListener)
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
            .listener(new StepExecutionListener() {
                @Override
                public ExitStatus afterStep(StepExecution stepExecution) {
                    long count = stepExecution.getWriteCount();
                    log.info("기사 백업 전체 완료 - 전체 백업된 기사 개수: {}", count);
                    return ExitStatus.COMPLETED;
                }
            })
            .build();
    }

    @Bean
    @StepScope
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
            Timer.Sample sample = Timer.start(monewMetrics.getMeterRegistry());
            String dateStr = LocalDate.now(ZoneId.of("Asia/Seoul")).minusDays(1)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String key = String.format("%s/backup-articles-%s.json", backupPrefix, dateStr);
            try {
                String json = objectMapper.writeValueAsString(articles);
                byte[] jsonBytes = json.getBytes(StandardCharsets.UTF_8);

                s3Util.upload(
                    backupBucket,
                    key,
                    new java.io.ByteArrayInputStream(jsonBytes),
                    jsonBytes.length,
                    "application/json"
                );
            } catch (Exception e) {
                log.error("백업 파일 업로드 중 오류 발생", e);
                throw new RuntimeException(e);
            } finally {
                sample.stop(monewMetrics.getBatchJobTimer("articleBackupJob"));
            }
        };
    }
}