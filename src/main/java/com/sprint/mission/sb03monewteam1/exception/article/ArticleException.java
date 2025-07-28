package com.sprint.mission.sb03monewteam1.exception.article;

import java.util.Map;

import com.sprint.mission.sb03monewteam1.exception.CustomException;
import com.sprint.mission.sb03monewteam1.exception.ErrorCode;

public class ArticleException extends CustomException {

    public ArticleException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ArticleException(ErrorCode errorCode, Map<String, String> details) {
        super(errorCode, details);
    }
}
