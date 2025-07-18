package com.sprint.mission.sb03monewteam1.fixture;

import com.sprint.mission.sb03monewteam1.dto.ArticleDto;
import com.sprint.mission.sb03monewteam1.entity.Article;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class ArticleFixture {

    // 기본값 상수
    public static final String DEFAULT_SOURCE = "네이버뉴스";
    public static final String DEFAULT_SOURCE_URL = "https://news.naver.com/article/001/123456";
    public static final String DEFAULT_TITLE = "테스트 기사 제목";
    public static final String DEFAULT_SUMMARY = "테스트 기사 요약 내용입니다.";
    public static final Long DEFAULT_VIEW_COUNT = 0L;
    public static final Long DEFAULT_COMMENT_COUNT = 0L;
    public static final Boolean DEFAULT_IS_DELETED = false;
    public static final Instant DEFAULT_PUBLISH_DATE = Instant.parse("2024-01-01T10:00:00Z");

    public static Article createArticle() {
        return Article.builder()
            .source(DEFAULT_SOURCE)
            .sourceUrl(DEFAULT_SOURCE_URL)
            .title(DEFAULT_TITLE)
            .publishDate(DEFAULT_PUBLISH_DATE)
            .summary(DEFAULT_SUMMARY)
            .viewCount(DEFAULT_VIEW_COUNT)
            .commentCount(DEFAULT_COMMENT_COUNT)
            .isDeleted(DEFAULT_IS_DELETED)
            .build();
    }

    public static Article createArticle(String source, String sourceUrl, String title) {
        return Article.builder()
            .source(source)
            .sourceUrl(sourceUrl)
            .title(title)
            .publishDate(DEFAULT_PUBLISH_DATE)
            .summary(DEFAULT_SUMMARY)
            .viewCount(DEFAULT_VIEW_COUNT)
            .commentCount(DEFAULT_COMMENT_COUNT)
            .isDeleted(DEFAULT_IS_DELETED)
            .build();
    }

    public static Article createArticleWithViewCount(Long viewCount) {
        return Article.builder()
            .source(DEFAULT_SOURCE)
            .sourceUrl(DEFAULT_SOURCE_URL)
            .title(DEFAULT_TITLE)
            .publishDate(DEFAULT_PUBLISH_DATE)
            .summary(DEFAULT_SUMMARY)
            .viewCount(viewCount)
            .commentCount(DEFAULT_COMMENT_COUNT)
            .isDeleted(DEFAULT_IS_DELETED)
            .build();
    }

    public static Article createArticleWithCommentCount(Long commentCount) {
        return Article.builder()
            .source(DEFAULT_SOURCE)
            .sourceUrl(DEFAULT_SOURCE_URL)
            .title(DEFAULT_TITLE)
            .publishDate(DEFAULT_PUBLISH_DATE)
            .summary(DEFAULT_SUMMARY)
            .viewCount(DEFAULT_VIEW_COUNT)
            .commentCount(commentCount)
            .isDeleted(DEFAULT_IS_DELETED)
            .build();
    }

    public static Article createDeletedArticle() {
        return Article.builder()
            .source(DEFAULT_SOURCE)
            .sourceUrl(DEFAULT_SOURCE_URL)
            .title(DEFAULT_TITLE)
            .publishDate(DEFAULT_PUBLISH_DATE)
            .summary(DEFAULT_SUMMARY)
            .viewCount(DEFAULT_VIEW_COUNT)
            .commentCount(DEFAULT_COMMENT_COUNT)
            .isDeleted(true)
            .build();
    }

    public static Article createArticleWithId(UUID id) {
        Article article = Article.builder()
            .source(DEFAULT_SOURCE)
            .sourceUrl(DEFAULT_SOURCE_URL)
            .title(DEFAULT_TITLE)
            .publishDate(DEFAULT_PUBLISH_DATE)
            .summary(DEFAULT_SUMMARY)
            .viewCount(DEFAULT_VIEW_COUNT)
            .commentCount(DEFAULT_COMMENT_COUNT)
            .isDeleted(DEFAULT_IS_DELETED)
            .build();
        article.setIdForTest(id); // 테스트 전용 id 세팅
        return article;
    }

    // ArticleDto 생성 메서드들
    public static ArticleDto createArticleDto() {
        return ArticleDto.builder()
            .id(UUID.randomUUID())
            .source(DEFAULT_SOURCE)
            .sourceUrl(DEFAULT_SOURCE_URL)
            .title(DEFAULT_TITLE)
            .publishDate(DEFAULT_PUBLISH_DATE)
            .summary(DEFAULT_SUMMARY)
            .viewCount(DEFAULT_VIEW_COUNT)
            .commentCount(DEFAULT_COMMENT_COUNT)
            .interests(List.of("테스트 관심사"))
            .createdAt(Instant.now())
            .build();
    }

    public static ArticleDto createArticleDto(String title, List<String> interests) {
        return ArticleDto.builder()
            .id(UUID.randomUUID())
            .source(DEFAULT_SOURCE)
            .sourceUrl(DEFAULT_SOURCE_URL)
            .title(title)
            .publishDate(DEFAULT_PUBLISH_DATE)
            .summary(DEFAULT_SUMMARY)
            .viewCount(DEFAULT_VIEW_COUNT)
            .commentCount(DEFAULT_COMMENT_COUNT)
            .interests(interests)
            .createdAt(Instant.now())
            .build();
    }

    public static List<ArticleDto> createArticleDtoList() {
        return List.of(
            createArticleDto("첫 번째 테스트 기사", List.of("기술", "IT")),
            createArticleDto("두 번째 테스트 기사", List.of("경제", "비즈니스")),
            createArticleDto("세 번째 테스트 기사", List.of("스포츠", "축구"))
        );
    }

    public static List<ArticleDto> createArticleDtoList(int count) {
        return List.of(
            createArticleDto("테스트 기사 " + (count + 1), List.of("테스트 관심사"))
        );
    }

    public static List<ArticleDto> createArticleDtoListWithInterests(List<String> interests) {
        return List.of(
            createArticleDto("관심사 관련 기사 1", interests),
            createArticleDto("관심사 관련 기사 2", interests),
            createArticleDto("관심사 관련 기사 3", interests)
        );
    }

}
