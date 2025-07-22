package com.sprint.mission.sb03monewteam1.batch.job;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.sb03monewteam1.config.LoadTestEnv;
import com.sprint.mission.sb03monewteam1.config.TestConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
@Disabled("CI 실행 방지, 테스트 시 주석 제거")
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
    private Job newsCollectJob;

    @Autowired
    private JobLauncher jobLauncher;

    @BeforeEach
    void setUp() {
        // 필요할 때마다 Job을 지정해서 사용
        jobLauncherTestUtils.setJobLauncher(jobLauncher);
    }

    @Test
    void newsCollectJob_실행_테스트() throws Exception {
        jobLauncherTestUtils.setJob(newsCollectJob);
        JobExecution jobExecution = jobLauncherTestUtils.launchJob();
        assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");
    }
}