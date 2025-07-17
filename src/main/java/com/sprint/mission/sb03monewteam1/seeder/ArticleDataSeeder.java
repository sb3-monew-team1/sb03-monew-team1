package com.sprint.mission.sb03monewteam1.seeder;

import com.sprint.mission.sb03monewteam1.entity.Article;
import com.sprint.mission.sb03monewteam1.repository.ArticleRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Profile({"dev", "postgres"})
@RequiredArgsConstructor
public class ArticleDataSeeder implements DataSeeder {

    private final ArticleRepository articleRepository;

    @Override
    @Transactional
    public void seed() {
        if (articleRepository.count() > 0) {
            log.info("ArticleDataSeeder: 기사가 이미 존재하여 시드를 실행하지 않습니다.");
            return;
        }

        for (int i = 1; i <= 5; i++) {
            Article article = createArticle("NAVER", "https://news.naver.com/article" + i,
                "샘플 기사" + i, "샘플 요약", Instant.now());
            articleRepository.save(article);
            articleRepository.flush();
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
