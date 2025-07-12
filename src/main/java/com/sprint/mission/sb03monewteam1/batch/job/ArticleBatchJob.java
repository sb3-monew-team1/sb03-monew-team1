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
public class ArticleBatchJob {

    @Bean
    public Job articleJob(JobRepository jobRepository, Step articleStep) {
        return new JobBuilder("articleJob", jobRepository)
                .start(articleStep)
                .build();
    }

    @Bean
    public Step articleStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("articleStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("Article Step executed!");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}