package com.sprint.mission.sb03monewteam1.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.sprint.mission.sb03monewteam1.config.LoadTestEnv;
import com.sprint.mission.sb03monewteam1.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@LoadTestEnv
@EnableScheduling
@SpringBootTest
@Import({TestConfig.class})
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ArticleCollectSchedulerTest {

    @MockitoBean
    private JobLauncher jobLauncher;

    @Autowired
    private Job newsCollectJob;

    @Autowired
    private ArticleCollectScheduler articleCollectScheduler;

    @Test
    void newsCollectJob_스케줄러_실행_테스트() throws Exception {
        articleCollectScheduler.runNewsCollectJob();
        verify(jobLauncher, times(1)).run(eq(newsCollectJob),
            any());
    }
}
