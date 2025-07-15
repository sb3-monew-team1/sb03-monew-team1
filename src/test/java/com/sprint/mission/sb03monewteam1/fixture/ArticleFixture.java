package com.sprint.mission.sb03monewteam1.fixture;

import com.sprint.mission.sb03monewteam1.entity.Article;
import java.time.Instant;
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

    // 기본 Article 생성
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

    // 커스텀 Article 생성
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

    // ID가 있는 Article 생성
    public static Article createArticleWithId(UUID id) {
        return Article.builder()
                .id(id)
                .source(DEFAULT_SOURCE)
                .sourceUrl(DEFAULT_SOURCE_URL + "/" + id)
                .title(DEFAULT_TITLE)
                .publishDate(DEFAULT_PUBLISH_DATE)
                .summary(DEFAULT_SUMMARY)
                .viewCount(DEFAULT_VIEW_COUNT)
                .commentCount(DEFAULT_COMMENT_COUNT)
                .isDeleted(DEFAULT_IS_DELETED)
                .build();
    }

    // 조회수가 있는 Article 생성
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

    // 댓글수가 있는 Article 생성
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

    // 삭제된 Article 생성
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
}
