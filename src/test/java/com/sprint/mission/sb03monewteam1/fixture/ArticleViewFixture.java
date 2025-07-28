package com.sprint.mission.sb03monewteam1.fixture;

import com.sprint.mission.sb03monewteam1.entity.Article;
import com.sprint.mission.sb03monewteam1.entity.ArticleView;
import java.util.UUID;

public class ArticleViewFixture {

    public static final UUID DEFAULT_USER_ID = UUID.randomUUID();

    public static ArticleView createArticleView() {
        return ArticleView.builder()
            .userId(DEFAULT_USER_ID)
            .article(ArticleFixture.createArticle())
            .build();
    }

    public static ArticleView createArticleView(UUID userId, Article article) {
        return ArticleView.builder()
            .userId(userId)
            .article(article)
            .build();
    }
}
