package com.sprint.mission.sb03monewteam1.fixture;

import com.sprint.mission.sb03monewteam1.entity.Article;
import com.sprint.mission.sb03monewteam1.entity.ArticleView;
import java.util.UUID;

public class ArticleViewFixture {

    // 기본값 상수
    public static final UUID DEFAULT_USER_ID = UUID.randomUUID();

    // 기본 ArticleView 생성
    public static ArticleView createArticleView() {
        return ArticleView.builder()
            .userId(DEFAULT_USER_ID)
            .article(ArticleFixture.createArticle())
            .build();
    }

    // 사용자 ID와 Article을 지정한 ArticleView 생성
    public static ArticleView createArticleView(UUID userId, Article article) {
        return ArticleView.builder()
            .userId(userId)
            .article(article)
            .build();
    }

    // ID가 있는 ArticleView 생성
    public static ArticleView createArticleViewWithId(UUID id, UUID userId, Article article) {
        return ArticleView.builder()
            .id(id)
            .userId(userId)
            .article(article)
            .build();
    }
}
