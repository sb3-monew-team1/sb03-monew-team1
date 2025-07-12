package com.sprint.mission.sb03monewteam1.config;

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
public class BatchConfig {

    @Bean
    public Job dummyJob(JobRepository jobRepository, Step dummyStep) {
        return new JobBuilder("dummyJob", jobRepository)
            .start(dummyStep)
            .build();
    }

    @Bean
    public Step dummyStep(JobRepository jobRepository,
        PlatformTransactionManager transactionManager) {
        return new StepBuilder("dummyStep", jobRepository)
            .tasklet((contribution, chunkContext) -> {
                System.out.println("Dummy Step executed!");
                return RepeatStatus.FINISHED;
            }, transactionManager)
            .build();
    }
}
