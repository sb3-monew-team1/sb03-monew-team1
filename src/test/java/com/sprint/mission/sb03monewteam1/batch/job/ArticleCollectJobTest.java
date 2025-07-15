package com.sprint.mission.sb03monewteam1.batch.job;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.sprint.mission.sb03monewteam1.config.LoadTestEnv;
import com.sprint.mission.sb03monewteam1.config.TestConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@ActiveProfiles("test")
@SpringBootTest
@LoadTestEnv
@Import({TestConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ArticleCollectJobTest {

    @TestConfiguration
    static class BatchTestConfig {

        @Bean
        public JobLauncherTestUtils jobLauncherTestUtils() {
            return new JobLauncherTestUtils();
        }
    }

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private Job articleCollectJob;

    @Autowired
    private JobLauncher jobLauncher;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils.setJob(articleCollectJob);
        jobLauncherTestUtils.setJobLauncher(jobLauncher);
    }

    @Test
    void articleCollectJob_실행_테스트() throws Exception {
        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob();

        // then
        assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");
    }

    @Test
    void articleCollectJob_각_Step_실행_테스트() throws Exception {
        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob();

        // then
        assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");

        assertThat(jobExecution.getStepExecutions())
            .extracting("stepName", "exitStatus.exitCode")
            .containsExactlyInAnyOrder(
                tuple("naverNewsCollectStep", "COMPLETED"),
                tuple("hankyungNewsCollectStep", "COMPLETED")
            );

        jobExecution.getStepExecutions().forEach(stepExecution -> {
            log.info("Step: " + stepExecution.getStepName());
            log.info("ReadCount: " + stepExecution.getReadCount());
            log.info("WriteCount: " + stepExecution.getWriteCount());
            log.info("SkipCount: " + stepExecution.getSkipCount());
        });
    }
}