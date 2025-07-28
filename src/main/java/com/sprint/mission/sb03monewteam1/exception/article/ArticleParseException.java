package com.sprint.mission.sb03monewteam1.exception.article;

import com.sprint.mission.sb03monewteam1.exception.ErrorCode;
import java.time.Instant;
import java.util.Map;

public class ArticleParseException extends ArticleException {

    public ArticleParseException(String message, String source, Exception cause) {
        super(ErrorCode.ARTICLE_COLLECTION_PARSING_ERROR, Map.of(
            "error", message,
            "source", source,
            "cause", cause != null ? cause.getMessage() : "",
            "timestamp", Instant.now().toString()
        ));
    }
}
