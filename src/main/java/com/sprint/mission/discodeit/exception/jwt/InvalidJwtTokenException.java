package com.sprint.mission.discodeit.exception.jwt;

import com.sprint.mission.discodeit.exception.ErrorCode;

public class InvalidJwtTokenException extends JwtException {
    public InvalidJwtTokenException() {
        super(ErrorCode.INVALID_JWT_TOKEN);
    }

    public InvalidJwtTokenException(Throwable cause) {
        super(ErrorCode.INVALID_JWT_TOKEN, cause);
    }
}