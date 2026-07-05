package com.sprint.mission.discodeit.exception.jwt;

import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ErrorCode;

public class JwtException extends DiscodeitException {
    public JwtException(ErrorCode errorCode) {
        super(errorCode);
    }

    public JwtException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}