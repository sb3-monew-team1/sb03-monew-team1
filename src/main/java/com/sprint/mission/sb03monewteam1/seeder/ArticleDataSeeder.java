package com.sprint.mission.sb03monewteam1.seeder;

import com.sprint.mission.sb03monewteam1.entity.Article;
import com.sprint.mission.sb03monewteam1.repository.ArticleRepository;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Profile("dev")
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

        List<Article> articles = List.of(
            createArticle("11111111-1111-1111-1111-111111111111", "NAVER",
                "https://news.naver.com/article1", "어제의 첫 번째 뉴스", "2025-07-12T09:00:00+09:00",
                "어제 첫 뉴스 요약", 3, 10, false, "2025-07-12T09:00:00+09:00"),
            createArticle("22222222-2222-2222-2222-222222222222", "DAUM",
                "https://news.daum.net/article2", "어제의 두 번째 뉴스", "2025-07-12T15:30:00+09:00",
                "어제 두 번째 뉴스 요약", 1, 5, false, "2025-07-13T15:30:00+09:00"),
            createArticle("33333333-3333-3333-3333-333333333333", "DAUM",
                "https://news.daum.net/article3", "어제의 세 번째 뉴스", "2025-07-12T16:00:00+09:00",
                "어제 세 번째 뉴스 요약", 2, 7, false, "2025-07-13T16:00:00+09:00"),
            createArticle("44444444-4444-4444-4444-444444444444", "NAVER",
                "https://news.naver.com/article4", "어제의 네 번째 뉴스", "2025-07-12T17:00:00+09:00",
                "어제 네 번째 뉴스 요약", 0, 2, false, "2025-07-14T17:00:00+09:00"),
            createArticle("55555555-5555-5555-5555-555555555555", "NAVER",
                "https://news.naver.com/article5", "어제의 다섯 번째 뉴스", "2025-07-12T18:00:00+09:00",
                "어제 다섯 번째 뉴스 요약", 5, 20, false, "2025-07-14T18:00:00+09:00"),
            createArticle("66666666-6666-6666-6666-666666666666", "DAUM",
                "https://news.daum.net/article6", "어제의 여섯 번째 뉴스", "2025-07-12T19:00:00+09:00",
                "어제 여섯 번째 뉴스 요약", 2, 8, false, "2025-07-14T19:00:00+09:00"),
            createArticle("77777777-7777-7777-7777-777777777777", "NAVER",
                "https://news.naver.com/article7", "어제의 일곱 번째 뉴스", "2025-07-12T20:00:00+09:00",
                "어제 일곱 번째 뉴스 요약", 4, 15, false, "2025-07-14T20:00:00+09:00"),
            createArticle("88888888-8888-8888-8888-888888888888", "DAUM",
                "https://news.daum.net/article8", "어제의 여덟 번째 뉴스", "2025-07-12T21:00:00+09:00",
                "어제 여덟 번째 뉴스 요약", 3, 12, false, "2025-07-15T21:00:00+09:00"),
            createArticle("99999999-9999-9999-9999-999999999999", "NAVER",
                "https://news.naver.com/article9", "어제의 아홉 번째 뉴스", "2025-07-12T22:00:00+09:00",
                "어제 아홉 번째 뉴스 요약", 1, 6, false, "2025-07-15T22:00:00+09:00"),
            createArticle("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa", "DAUM",
                "https://news.daum.net/article10", "어제의 열 번째 뉴스", "2025-07-12T23:00:00+09:00",
                "어제 열 번째 뉴스 요약", 0, 3, false, "2025-07-15T23:00:00+09:00")
        );

        articleRepository.saveAll(articles);
        log.info("ArticleDataSeeder: 총 {}개의 기사 시드 데이터가 추가되었습니다.", articles.size());
    }

    private Article createArticle(
        String id,
        String source,
        String sourceUrl,
        String title,
        String publishDate,
        String summary,
        int commentCount,
        int viewCount,
        boolean isDeleted,
        String createdAt
    ) {
        return Article.builder()
            .source(source)
            .sourceUrl(sourceUrl)
            .title(title)
            .publishDate(OffsetDateTime.parse(publishDate).toInstant())
            .summary(summary)
            .commentCount((long) commentCount)
            .viewCount((long) viewCount)
            .isDeleted(isDeleted)
            .build();
    }
}
