package com.sprint.mission.sb03monewteam1.scheduler;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.sprint.mission.sb03monewteam1.collector.HankyungNewsCollector;
import com.sprint.mission.sb03monewteam1.collector.NaverNewsCollector;
import com.sprint.mission.sb03monewteam1.config.LoadTestEnv;
import com.sprint.mission.sb03monewteam1.entity.Interest;

@LoadTestEnv
@EnableScheduling
@SpringBootTest
@ActiveProfiles("test")
class ArticleBatchSchedulerTest {

    @MockitoBean
    private JobLauncher jobLauncher;

    @MockitoBean
    private NaverNewsCollector naverNewsCollector;

    @MockitoBean
    private HankyungNewsCollector hankyungNewsCollector;

    @Autowired
    private ArticleBatchScheduler articleBatchScheduler;

    @Test
    void 네이버_뉴스_수집_메서드가_호출된다() throws Exception {
        Interest dummyInterest = mock(Interest.class);
        String dummyKeyword = "감자";
        articleBatchScheduler.collectNaverNews(dummyInterest, dummyKeyword);
        verify(naverNewsCollector, times(1)).collect(eq(dummyInterest), eq(dummyKeyword));
    }

    @Test
    void 한국경제_뉴스_수집_메서드가_호출된다() throws Exception {
        Interest dummyInterest = mock(Interest.class);
        String dummyKeyword = "고구마";
        articleBatchScheduler.collectHankyungNews(dummyInterest, dummyKeyword);
        verify(hankyungNewsCollector, times(1)).collect(eq(dummyInterest), eq(dummyKeyword));
    }
}
