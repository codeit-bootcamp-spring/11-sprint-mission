package com.sprint.mission.discodeit.exception.jwt;

import com.sprint.mission.discodeit.exception.ErrorCode;

public class JwtMalformedException extends JwtException {

    public JwtMalformedException(Throwable cause) {
        super(ErrorCode.JWT_MALFORMED, cause);
    }
}
