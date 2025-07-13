package com.sprint.mission.sb03monewteam1.exception.article;

import java.util.Map;

import com.sprint.mission.sb03monewteam1.exception.ErrorCode;

public class DuplicateArticleViewException extends ArticleException {

    public DuplicateArticleViewException(String userId, String articleId) {
        super(ErrorCode.DUPLICATE_ARTICLE_VIEW, Map.of("userId", userId, "articleId", articleId));
    }

    public DuplicateArticleViewException() {
        super(ErrorCode.DUPLICATE_ARTICLE_VIEW);
    }
}
