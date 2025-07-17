package com.sprint.mission.sb03monewteam1.seeder;

import com.sprint.mission.sb03monewteam1.entity.Article;
import com.sprint.mission.sb03monewteam1.repository.ArticleRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile({"dev", "postgres"})
@RequiredArgsConstructor
public class ArticleDataSeeder implements DataSeeder {

    private final ArticleRepository articleRepository;

    @Override
    public void seed() {

        for (int i = 1; i <= 5; i++) {
            String sourceUrl = "https://news.naver.com/article" + i;
            if (!articleRepository.existsBySourceUrl(sourceUrl)) {
                Article article = createArticle("NAVER", sourceUrl, "샘플 기사" + i, "샘플 요약",
                    Instant.now());
                articleRepository.save(article);
            }
        }

        log.info("샘플 기사 5개 생성 완료");
    }

    private Article createArticle(String source, String sourceUrl, String title, String summary,
        Instant publishDate) {

        return Article.builder()
            .source(source)
            .sourceUrl(sourceUrl)
            .title(title)
            .summary(summary)
            .publishDate(publishDate)
            .build();
    }
}
