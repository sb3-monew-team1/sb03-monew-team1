package com.sprint.mission.sb03monewteam1.exception.article;

import com.sprint.mission.sb03monewteam1.exception.ErrorCode;

public class ArticleCollectException extends ArticleException {
    public ArticleCollectException(String message) {
        super(ErrorCode.INTERNAL_SERVER_ERROR, java.util.Map.of("error", message));
    }
}
