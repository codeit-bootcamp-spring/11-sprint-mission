package com.sprint.mission.discodeit.exception.jwt;

import com.sprint.mission.discodeit.exception.ErrorCode;

public class JwtExpiredException extends JwtException {

    public JwtExpiredException() {
        super(ErrorCode.JWT_EXPIRED);
    }
}
