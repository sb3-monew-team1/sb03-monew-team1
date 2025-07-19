package com.sprint.mission.sb03monewteam1.config.metric;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.sprint.mission.sb03monewteam1.repository.jpa.ArticleRepository;
import com.sprint.mission.sb03monewteam1.repository.jpa.InterestRepository;
import com.sprint.mission.sb03monewteam1.repository.jpa.UserRepository;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MonewMetrics {

    private final MeterRegistry meterRegistry;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final InterestRepository interestRepository;

    @Getter
    private Counter articleCreatedCounter;
    @Getter
    private Counter userCreatedCounter;
    @Getter
    private Counter interestCreatedCounter;

    @Getter
    private final Map<UUID, Counter> interestArticleMappedCounters = new ConcurrentHashMap<>();
    @Getter
    private final Map<UUID, Counter> articleViewedCounters = new ConcurrentHashMap<>();
    @Getter
    private final Map<UUID, Counter> articleCommentedCounters = new ConcurrentHashMap<>();
    @Getter
    private final Map<String, Timer> batchJobTimers = new ConcurrentHashMap<>();

    public MonewMetrics(MeterRegistry meterRegistry,
        ArticleRepository articleRepository,
        UserRepository userRepository,
        InterestRepository interestRepository) {
        this.meterRegistry = meterRegistry;
        this.articleRepository = articleRepository;
        this.userRepository = userRepository;
        this.interestRepository = interestRepository;
    }

    @PostConstruct
    public void initMetrics() {
        String systemTag = "monew";

        articleCreatedCounter = Counter.builder("monew.article.created")
            .description("생성된 뉴스 기사 수")
            .tag("system", systemTag)
            .register(meterRegistry);

        userCreatedCounter = Counter.builder("monew.user.created")
            .description("생성된 유저 수")
            .tag("system", systemTag)
            .register(meterRegistry);

        interestCreatedCounter = Counter.builder("monew.interest.created")
            .description("생성된 관심사 수")
            .tag("system", systemTag)
            .register(meterRegistry);

        Gauge.builder("monew.article.total", articleRepository, ArticleRepository::count)
            .description("전체 뉴스 기사 수")
            .tag("system", systemTag)
            .register(meterRegistry);

        Gauge.builder("monew.user.total", userRepository, UserRepository::count)
            .description("전체 유저 수")
            .tag("system", systemTag)
            .register(meterRegistry);

        Gauge.builder("monew.interest.total", interestRepository, InterestRepository::count)
            .description("전체 관심사 수")
            .tag("system", systemTag)
            .register(meterRegistry);
    }

    public void recordJobSuccess(String jobName) {
        meterRegistry.counter("monew.batch.job.success", "system", "monew", "job", jobName)
            .increment();
    }

    public void recordJobFailure(String jobName) {
        meterRegistry.counter("monew.batch.job.failure", "system", "monew", "job", jobName)
            .increment();
    }

    public Counter getInterestArticleMappedCounter(UUID interestId, String interestName) {
        return interestArticleMappedCounters.computeIfAbsent(interestId, id ->
            Counter.builder("monew.interest.article.mapped")
                .description("관심사별 매핑된 뉴스 기사 수")
                .tag("interestId", interestId.toString())
                .tag("interestName", interestName)
                .register(meterRegistry)
        );
    }

    public Counter getArticleViewedCounter(UUID articleId) {
        return articleViewedCounters.computeIfAbsent(articleId, id ->
            Counter.builder("monew.article.viewed")
                .description("기사별 조회수")
                .tag("articleId", articleId.toString())
                .register(meterRegistry)
        );
    }

    public Counter getArticleCommentedCounter(UUID articleId) {
        return articleCommentedCounters.computeIfAbsent(articleId, id ->
            Counter.builder("monew.article.commented")
                .description("기사별 댓글수")
                .tag("articleId", articleId.toString())
                .register(meterRegistry)
        );
    }

    public Timer getBatchJobTimer(String jobName) {
        return batchJobTimers.computeIfAbsent(jobName, name ->
            Timer.builder("monew.batch.job.duration")
                .description("배치 작업별 소요 시간")
                .tag("job", jobName)
                .register(meterRegistry)
        );
    }

    public MeterRegistry getMeterRegistry() {
        return meterRegistry;
    }
}
