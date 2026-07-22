package com.sprint.mission.discodeit.exception.jwt;

import com.sprint.mission.discodeit.exception.ErrorCode;

public class RefreshTokenInvalidException extends JwtException {

    public RefreshTokenInvalidException() {
        super(ErrorCode.REFRESH_TOKEN_INVALID);
    }
}
