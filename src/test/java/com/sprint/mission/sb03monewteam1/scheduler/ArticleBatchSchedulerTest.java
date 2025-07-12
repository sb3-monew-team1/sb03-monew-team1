package com.sprint.mission.sb03monewteam1.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.sprint.mission.sb03monewteam1.config.LoadTestEnv;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@LoadTestEnv
@EnableScheduling
@SpringBootTest
@ActiveProfiles("test")
class ArticleBatchSchedulerTest {

    @MockitoBean
    private JobLauncher jobLauncher;

    @Autowired
    private Job articleJob;

    @Autowired
    private ArticleBatchScheduler articleBatchScheduler;

    @Test
    void 스케줄러가_주기적으로_배치잡을_실행한다() throws Exception {
        articleBatchScheduler.runArticleJob();

        verify(jobLauncher, times(1)).run(eq(articleJob), any());
    }
}
