package com.sprint.mission.discodeit.exception.jwt;

import com.sprint.mission.discodeit.exception.ErrorCode;

public class JwtGenerationFailedException extends JwtException {

    public JwtGenerationFailedException(Throwable cause) {
        super(ErrorCode.JWT_GENERATION_FAILED, cause);
    }
}
