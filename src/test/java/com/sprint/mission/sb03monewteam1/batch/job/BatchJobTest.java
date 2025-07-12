package com.sprint.mission.sb03monewteam1.batch;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBatchTest
@SpringBootTest
class BatchJobTest {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job dummyJob;

    @Test
    void BatchJob_실행_테스트() throws Exception {
        var jobExecution = jobLauncher.run(dummyJob, new org.springframework.batch.core.JobParameters());
        assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");
    }
}
