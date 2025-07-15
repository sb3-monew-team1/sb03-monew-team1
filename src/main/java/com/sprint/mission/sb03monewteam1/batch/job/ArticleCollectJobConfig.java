package com.sprint.mission.sb03monewteam1.batch.job;

import com.sprint.mission.sb03monewteam1.collector.HankyungNewsCollector;
import com.sprint.mission.sb03monewteam1.collector.NaverNewsCollector;
import com.sprint.mission.sb03monewteam1.dto.CollectedArticleDto;
import com.sprint.mission.sb03monewteam1.entity.Article;
import com.sprint.mission.sb03monewteam1.entity.InterestKeyword;
import com.sprint.mission.sb03monewteam1.exception.article.ArticleCollectException;
import com.sprint.mission.sb03monewteam1.repository.ArticleRepository;
import com.sprint.mission.sb03monewteam1.repository.InterestKeywordRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.IteratorItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
@Slf4j
@EnableBatchProcessing
public class ArticleCollectJobConfig {

    private final InterestKeywordRepository interestKeywordRepository;
    private final NaverNewsCollector naverNewsCollector;
    private final HankyungNewsCollector hankyungNewsCollector;
    private final ArticleRepository articleRepository;

    @Bean
    public Job articleCollectJob(
        JobRepository jobRepository,
        Step naverNewsCollectStep,
        Step hankyungNewsCollectStep
    ) {
        return new JobBuilder("articleCollectJob", jobRepository)
            .start(naverNewsCollectStep)
            .next(hankyungNewsCollectStep)
            .build();
    }

    @Bean
    public Step naverNewsCollectStep(JobRepository jobRepository,
        PlatformTransactionManager transactionManager) {
        return new StepBuilder("naverNewsCollectStep", jobRepository)
            .<String, List<Article>>chunk(10, transactionManager)
            .reader(keywordReader())
            .processor(naverNewsCollectProcessor())
            .writer(articleListWriter())
            .faultTolerant()
            .skipLimit(10)
            .skip(ArticleCollectException.class)
            .build();
    }

    @Bean
    public Step hankyungNewsCollectStep(JobRepository jobRepository,
        PlatformTransactionManager transactionManager) {
        return new StepBuilder("hankyungNewsCollectStep", jobRepository)
            .<String, List<Article>>chunk(10, transactionManager)
            .reader(keywordReader())
            .processor(hankyungNewsCollectProcessor())
            .writer(articleListWriter())
            .faultTolerant()
            .skipLimit(10)
            .skip(ArticleCollectException.class)
            .build();
    }

    @Bean
    public ItemReader<String> keywordReader() {
        List<String> keywords = interestKeywordRepository.findAll()
            .stream()
            .map(InterestKeyword::getKeyword)
            .distinct()
            .collect(Collectors.toList());
        return new IteratorItemReader<>(keywords);
    }

    private List<Article> toArticles(List<CollectedArticleDto> dtos) {
        return dtos.stream()
            .filter(dto -> !articleRepository.existsBySourceUrl(dto.sourceUrl()))
            .map(dto -> Article.builder()
                .source(dto.source())
                .sourceUrl(dto.sourceUrl())
                .title(dto.title())
                .publishDate(dto.publishDate())
                .summary(dto.summary())
                .viewCount(0L)
                .commentCount(0L)
                .isDeleted(false)
                .build())
            .collect(Collectors.toList());
    }

    @Bean
    public ItemProcessor<String, List<Article>> naverNewsCollectProcessor() {
        return keyword -> toArticles(naverNewsCollector.collect(null, keyword));
    }

    @Bean
    public ItemProcessor<String, List<Article>> hankyungNewsCollectProcessor() {
        return keyword -> toArticles(hankyungNewsCollector.collect(null, keyword));
    }

    @Bean
    public ItemWriter<List<Article>> articleListWriter() {
        return items -> {
            for (List<Article> articles : items) {
                if (!articles.isEmpty()) {
                    articleRepository.saveAll(articles);
                }
            }
        };
    }
}
