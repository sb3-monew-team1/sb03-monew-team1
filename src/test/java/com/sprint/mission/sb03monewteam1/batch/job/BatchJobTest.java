package com.sprint.mission.sb03monewteam1.batch.job;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import com.sprint.mission.sb03monewteam1.config.TestConfig;
import com.sprint.mission.sb03monewteam1.config.TestEnvSetup;

@SpringBatchTest
@SpringBootTest
@Import(TestConfig.class)
class BatchJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @BeforeAll
    static void setUp() {
        TestEnvSetup.loadEnvVariables();
    }

    @Test
    void dummyJob_실행_테스트() throws Exception {
        var jobExecution = jobLauncherTestUtils.launchJob();
        assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");
    }
}
