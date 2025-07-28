package com.sprint.mission.sb03monewteam1.batch.job;

import com.sprint.mission.sb03monewteam1.config.metric.MonewMetrics;
import com.sprint.mission.sb03monewteam1.dto.ArticleWithKeyword;
import com.sprint.mission.sb03monewteam1.entity.Article;
import com.sprint.mission.sb03monewteam1.event.listener.NewsCollectJobCompletionListener;
import com.sprint.mission.sb03monewteam1.exception.article.ArticleCollectException;
import com.sprint.mission.sb03monewteam1.service.ArticleService;
import jakarta.persistence.EntityManagerFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ArticleCollectJobConfig {

    private final ArticleService articleService;
    private final ApplicationEventPublisher eventPublisher;
    private final MonewMetrics monewMetrics;

    @Bean(name = "batchTaskExecutor")
    public TaskExecutor batchTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(100);
        executor.initialize();
        return executor;
    }

    @Configuration
    public class RateLimiterConfig {

    }

    @Bean
    public JobExecutionListener newsCollectJobExecutionListener() {
        return new NewsCollectJobCompletionListener(eventPublisher, monewMetrics,
            "newsCollectJob");
    }

    @Bean
    public Job newsCollectJob(
        JobRepository jobRepository,
        Step naverNewsCollectStep,
        Step hankyungNewsCollectStep,
        JobExecutionListener newsCollectJobExecutionListener,
        @Qualifier("batchTaskExecutor") TaskExecutor taskExecutor
) {
        Flow naverFlow = new FlowBuilder<Flow>("naverFlow")
            .start(naverNewsCollectStep)
            .build();

        Flow hankyungFlow = new FlowBuilder<Flow>("hankyungFlow")
            .start(hankyungNewsCollectStep)
            .build();

        return new JobBuilder("newsCollectJob", jobRepository)
            .start(new FlowBuilder<Flow>("splitFlow")
                .split(taskExecutor)
                .add(naverFlow, hankyungFlow)
                .build())
            .end()
            .listener(newsCollectJobExecutionListener)
            .build();
    }

    // Naver
    @Bean
    public Step naverNewsCollectStep(
        JobRepository jobRepository,
        PlatformTransactionManager transactionManager,
        EntityManagerFactory entityManagerFactory,
        @Qualifier("batchTaskExecutor") TaskExecutor taskExecutor
    ) {
        return new StepBuilder("naverNewsCollectStep", jobRepository)
            .<String, ArticleWithKeyword>chunk(10, transactionManager)
            .reader(distinctKeywordReader(entityManagerFactory))
            .processor(naverNewsCollectProcessor())
            .writer(articleWithKeywordWriter())
            .faultTolerant()
            .skipLimit(10)
            .skip(ArticleCollectException.class)
            .skip(NullPointerException.class)
            .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<String> distinctKeywordReader(
        EntityManagerFactory entityManagerFactory) {

        return new JpaPagingItemReaderBuilder<String>()
            .name("distinctKeywordReader")
            .entityManagerFactory(entityManagerFactory)
            .pageSize(100)
            .queryString("SELECT DISTINCT k.keyword FROM InterestKeyword k ORDER BY k.keyword ASC")
            .build();
    }

    @Bean
    public ItemProcessor<String, ArticleWithKeyword> naverNewsCollectProcessor() {
        return keyword -> {
            List<Article> articles = articleService.collectNaverArticles(keyword);
            return new ArticleWithKeyword(articles, keyword);
        };
    }

    // Hankyung
    @Bean
    public Step hankyungNewsCollectStep(
        JobRepository jobRepository,
        PlatformTransactionManager transactionManager,
        EntityManagerFactory entityManagerFactory,
        @Qualifier("batchTaskExecutor") TaskExecutor taskExecutor
    ) {
        return new StepBuilder("hankyungNewsCollectStep", jobRepository)
            .<String, ArticleWithKeyword>chunk(10, transactionManager)
            .reader(distinctKeywordReader(entityManagerFactory))
            .processor(hankyungNewsCollectProcessor())
            .writer(articleWithKeywordWriter())
            .faultTolerant()
            .skipLimit(10)
            .skip(ArticleCollectException.class)
            .skip(NullPointerException.class)
            .build();
    }

    @Bean
    public ItemProcessor<String, ArticleWithKeyword> hankyungNewsCollectProcessor() {
        return keyword -> {
            List<Article> articles = articleService.collectHankyungArticles(keyword);
            return new ArticleWithKeyword(articles, keyword);
        };
    }

    // 저장 공통
    @Bean
    public ItemWriter<ArticleWithKeyword> articleWithKeywordWriter() {
        return items -> {
            for (ArticleWithKeyword aw : items) {
                if (aw.articles() != null && !aw.articles().isEmpty()) {
                    articleService.saveArticles(aw.articles(), aw.keyword());
                }
            }
        };
    }
}
