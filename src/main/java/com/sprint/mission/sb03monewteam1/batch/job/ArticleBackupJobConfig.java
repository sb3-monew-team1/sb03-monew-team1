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

@Configuration
public class ArticleBackupJobConfig {

    @Bean
    public Job articleBackupJob(JobRepository jobRepository, Step articleBackupStep) {
        return new JobBuilder("articleBackupJob", jobRepository)
            .start(articleBackupStep)
            .build();
    }

    @Bean
    public Step articleBackupStep(JobRepository jobRepository,
        PlatformTransactionManager transactionManager) {
        return new StepBuilder("articleBackupStep", jobRepository)
            .tasklet((contribution, chunkContext) -> {
                System.out.println("Article Backup Step executed!");
                return RepeatStatus.FINISHED;
            }, transactionManager)
            .build();
    }
}