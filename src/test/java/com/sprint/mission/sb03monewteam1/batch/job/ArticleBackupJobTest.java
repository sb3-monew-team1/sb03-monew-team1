package com.sprint.mission.sb03monewteam1.batch.job;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.sb03monewteam1.config.metric.MonewMetrics;
import com.sprint.mission.sb03monewteam1.dto.ArticleDto;
import com.sprint.mission.sb03monewteam1.fixture.ArticleFixture;
import com.sprint.mission.sb03monewteam1.util.S3Util;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@Slf4j
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class ArticleBackupJobTest {

    @Mock
    S3Util s3Util;
    @Mock
    MonewMetrics monewMetrics;
    @Mock
    Timer.Sample sample;
    @Mock
    ObjectMapper objectMapper;
    @Mock
    Timer timer;

    @InjectMocks
    ArticleBackupJobConfig config;

    // MeterRegistry 테스트는 Mock 말고 반드시 실제 객체를 사용할 것...

    @Test
    void writer가_S3Util_upload를_호출() throws Exception {
        List<ArticleDto> articles = List.of(ArticleFixture.createArticleDto());
        String json = "[{\"id\":\"1\",\"source\":\"test\"}]";
        byte[] jsonBytes = json.getBytes(StandardCharsets.UTF_8);

        when(objectMapper.writeValueAsString(any())).thenReturn(json);
        when(monewMetrics.getMeterRegistry()).thenReturn(new SimpleMeterRegistry());
        when(monewMetrics.getBatchJobTimer(anyString())).thenReturn(timer);

        ReflectionTestUtils.setField(config, "backupBucket", "test-bucket");
        ReflectionTestUtils.setField(config, "backupPrefix", "articles");

        ItemWriter<ArticleDto> writer = config.writer();
        writer.write(new Chunk<>(articles));

        verify(s3Util).upload(
            eq("test-bucket"),
            contains("articles/backup-articles-"),
            any(InputStream.class),
            eq((long) jsonBytes.length),
            eq("application/json")
        );
    }

    @Test
    void SimpleMeterRegistry를_사용해서_메트릭_동작을_검증() {
        var meterRegistry = new SimpleMeterRegistry();
        Metrics.addRegistry(meterRegistry);

        meterRegistry.counter("test_count").increment();

        var actual = meterRegistry.counter("test_count").count();
        assertEquals(1.0d, actual);
    }
}