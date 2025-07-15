package com.sprint.mission.sb03monewteam1.batch.job;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.sb03monewteam1.config.LoadTestEnv;
import com.sprint.mission.sb03monewteam1.config.TestConfig;
import com.sprint.mission.sb03monewteam1.util.S3Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
@LoadTestEnv
@Import({TestConfig.class, ArticleBackupJobTest.S3MockConfig.class})
class ArticleBackupJobTest {

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
    private Job articleBackupJob;

    @Autowired
    private JobLauncher jobLauncher;

    @TestConfiguration
    static class S3MockConfig {

        @Bean
        public S3Util s3Util() {
            return Mockito.mock(S3Util.class);
        }
    }

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils.setJob(articleBackupJob);
        jobLauncherTestUtils.setJobLauncher(jobLauncher);
    }

    @Test
    void articleBackupJob_실행_테스트() throws Exception {
        var jobExecution = jobLauncherTestUtils.launchJob();

        assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");
    }
}