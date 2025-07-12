package com.sprint.mission.sb03monewteam1.batch.job;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Configuration
public class ArticleBackupJobConfig {

    @Bean
    public Job articleBackupJob(JobRepository jobRepository, Step articleBackupStep) {
        return new JobBuilder("articleBackupJob", jobRepository)
            .start(articleBackupStep)
            .build();
    }

    @Bean
    public Step articleBackupStep(
        JobRepository jobRepository,
        PlatformTransactionManager transactionManager,
        S3Client s3Client) {
        return new StepBuilder("articleBackupStep", jobRepository)
            .tasklet((contribution, chunkContext) -> {
                PutObjectRequest request = PutObjectRequest.builder()
                    .bucket("test-bucket")
                    .key("backup.txt")
                    .build();
                RequestBody body = RequestBody.fromString("backup data");
                s3Client.putObject(request, body);

                System.out.println("Article Backup Step executed!");
                return RepeatStatus.FINISHED;
            }, transactionManager)
            .build();
    }
}