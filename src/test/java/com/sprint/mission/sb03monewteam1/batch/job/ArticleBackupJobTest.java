package com.sprint.mission.sb03monewteam1.batch.job;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import com.sprint.mission.sb03monewteam1.config.LoadTestEnv;
import com.sprint.mission.sb03monewteam1.config.TestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@SpringBootTest
@LoadTestEnv
@Import(TestConfig.class)
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

    @MockitoBean
    private S3Client s3Client;

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

    @Test
    void articleBackupJob_실행시_S3업로드_호출됨() throws Exception {
        jobLauncherTestUtils.launchJob();

        verify(s3Client, atLeastOnce()).putObject(
            any(PutObjectRequest.class),
            any(RequestBody.class));
    }
}