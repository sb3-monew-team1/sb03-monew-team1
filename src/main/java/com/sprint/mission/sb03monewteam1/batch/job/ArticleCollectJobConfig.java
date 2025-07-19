package com.sprint.mission.sb03monewteam1.batch.job;

import com.sprint.mission.sb03monewteam1.dto.ArticleWithKeyword;
import com.sprint.mission.sb03monewteam1.entity.Article;
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
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ArticleCollectJobConfig {

    private final ArticleService articleService;
    private final ApplicationEventPublisher eventPublisher;

    @Bean
    public JobExecutionListener naverNewsCollectJobExecutionListener() {
        return new NewsCollectJobCompletionListener(eventPublisher, "naverNewsCollectJob");
    }

    @Bean
    public JobExecutionListener hankyungNewsCollectJobExecutionListener() {
        return new NewsCollectJobCompletionListener(eventPublisher, "hankyungNewsCollectJob");
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

    // Naver
    @Bean
    public Step naverNewsCollectStep(
        JobRepository jobRepository,
        PlatformTransactionManager transactionManager,
        EntityManagerFactory entityManagerFactory
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
    public Job naverNewsCollectJob(
        JobRepository jobRepository,
        Step naverNewsCollectStep,
        JobExecutionListener naverNewsCollectJobExecutionListener
    ) {
        return new JobBuilder("naverNewsCollectJob", jobRepository)
            .start(naverNewsCollectStep)
            .listener(naverNewsCollectJobExecutionListener)
            .build();
    }


    @Bean
    public ItemProcessor<String, ArticleWithKeyword> naverNewsCollectProcessor() {
        return keyword -> {
            Thread.sleep(100);
            List<Article> articles = articleService.collectNaverArticles(keyword);
            return new ArticleWithKeyword(articles, keyword);
        };
    }

    // Hankyung
    @Bean
    public Step hankyungNewsCollectStep(
        JobRepository jobRepository,
        PlatformTransactionManager transactionManager,
        EntityManagerFactory entityManagerFactory
    ) {
        return new StepBuilder("hankyungNewsCollectStep", jobRepository)
            .<String, ArticleWithKeyword>chunk(10, transactionManager)
            .reader(distinctKeywordReader(entityManagerFactory))
            .processor(hankyungNewsCollectProcessor())
            .writer(articleWithKeywordWriter())
            .faultTolerant()
            .skipLimit(10)
            .skip(ArticleCollectException.class)
            .build();
    }

    @Bean
    public Job hankyungNewsCollectJob(
        JobRepository jobRepository,
        Step hankyungNewsCollectStep,
        JobExecutionListener hankyungNewsCollectJobExecutionListener
    ) {
        return new JobBuilder("hankyungNewsCollectJob", jobRepository)
            .start(hankyungNewsCollectStep)
            .listener(hankyungNewsCollectJobExecutionListener)
            .build();
    }

    @Bean
    public ItemProcessor<String, ArticleWithKeyword> hankyungNewsCollectProcessor() {
        return keyword -> {
            Thread.sleep(100);
            List<Article> articles = articleService.collectNaverArticles(keyword);
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
