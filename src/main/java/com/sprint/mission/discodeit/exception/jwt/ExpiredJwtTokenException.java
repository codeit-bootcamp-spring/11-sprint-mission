package com.sprint.mission.discodeit.exception.jwt;

import com.sprint.mission.discodeit.exception.ErrorCode;

public class ExpiredJwtTokenException extends JwtException {
    public ExpiredJwtTokenException() {
        super(ErrorCode.EXPIRED_JWT_TOKEN);
    }
}