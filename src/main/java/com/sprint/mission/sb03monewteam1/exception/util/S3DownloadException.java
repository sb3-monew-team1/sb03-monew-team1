package com.sprint.mission.sb03monewteam1.exception.util;

import com.sprint.mission.sb03monewteam1.exception.ErrorCode;
import java.util.Map;

public class S3DownloadException extends S3Exception {

    public S3DownloadException(String cause) {
        super(ErrorCode.S3_DOWNLOAD_FAILED, Map.of("cause", cause));
    }
}
