package com.sprint.mission.sb03monewteam1.exception.util;

import com.sprint.mission.sb03monewteam1.exception.ErrorCode;
import java.util.Map;

public class S3UploadException extends S3Exception {

    public S3UploadException(String cause) {
        super(ErrorCode.S3_UPLOAD_FAILED, Map.of("cause", cause));
    }
}
