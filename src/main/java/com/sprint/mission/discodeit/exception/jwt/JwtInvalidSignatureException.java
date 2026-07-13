package com.sprint.mission.discodeit.exception.jwt;

import com.sprint.mission.discodeit.exception.ErrorCode;

public class JwtInvalidSignatureException extends JwtException {

    public JwtInvalidSignatureException() {
        super(ErrorCode.JWT_INVALID_SIGNATURE);
    }
}
