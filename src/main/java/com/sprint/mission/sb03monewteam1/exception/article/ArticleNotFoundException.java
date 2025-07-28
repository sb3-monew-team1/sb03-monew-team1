package com.sprint.mission.sb03monewteam1.exception.article;

import java.util.Map;

import com.sprint.mission.sb03monewteam1.exception.ErrorCode;

public class ArticleNotFoundException extends ArticleException {

    public ArticleNotFoundException(String articleId) {
        super(ErrorCode.ARTICLE_NOT_FOUND, Map.of("articleId", articleId));
    }

    public ArticleNotFoundException() {
        super(ErrorCode.ARTICLE_NOT_FOUND);
    }
}
