package com.sprint.mission.sb03monewteam1.batch.job;

import com.sprint.mission.sb03monewteam1.entity.Article;
import com.sprint.mission.sb03monewteam1.entity.InterestKeyword;
import com.sprint.mission.sb03monewteam1.exception.article.ArticleCollectException;
import com.sprint.mission.sb03monewteam1.repository.ArticleRepository;
import com.sprint.mission.sb03monewteam1.repository.InterestKeywordRepository;
import com.sprint.mission.sb03monewteam1.service.ArticleService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
@Slf4j
@EnableBatchProcessing
public class ArticleCollectJobConfig {

    private final InterestKeywordRepository interestKeywordRepository;
    private final ArticleRepository articleRepository;
    private final ArticleService articleService;

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
            .<InterestKeyword, List<Article>>chunk(10, transactionManager)
            .reader(keywordReader())
            .processor(naverNewsCollectProcessor())
            .writer(articleListWriter())
            .faultTolerant()
            .skipLimit(10)
            .skip(ArticleCollectException.class)
            .skip(NullPointerException.class)
            .build();
    }

    @Bean
    public Step hankyungNewsCollectStep(JobRepository jobRepository,
        PlatformTransactionManager transactionManager) {
        return new StepBuilder("hankyungNewsCollectStep", jobRepository)
            .<InterestKeyword, List<Article>>chunk(10, transactionManager)
            .reader(keywordReader())
            .processor(hankyungNewsCollectProcessor())
            .writer(articleListWriter())
            .faultTolerant()
            .skipLimit(10)
            .skip(ArticleCollectException.class)
            .build();
    }

    @Bean
    public RepositoryItemReader<InterestKeyword> keywordReader() {
        return new RepositoryItemReaderBuilder<InterestKeyword>()
            .name("keywordReader")
            .repository(interestKeywordRepository)
            .methodName("findAll")
            .pageSize(100)
            .arguments()
            .sorts(Map.of("id", Sort.Direction.ASC))
            .build();
    }

    @Bean
    public ItemProcessor<InterestKeyword, List<Article>> naverNewsCollectProcessor() {
        return interestKeyword -> {
            articleService.collectAndSaveNaverArticles(
                interestKeyword.getInterest(),
                interestKeyword.getKeyword()
            );
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Thread interrupted during sleep", e);
            }
            return List.of();
        };
    }

    @Bean
    public ItemProcessor<InterestKeyword, List<Article>> hankyungNewsCollectProcessor() {
        return interestKeyword -> {
            articleService.collectAndSaveHankyungArticles(
                interestKeyword.getInterest(),
                interestKeyword.getKeyword()
            );
            return List.of();
        };
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

    @Bean
    public TaskExecutor taskExecutor() {
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("batch-task-");
        executor.setConcurrencyLimit(10);
        return executor;
    }
}
