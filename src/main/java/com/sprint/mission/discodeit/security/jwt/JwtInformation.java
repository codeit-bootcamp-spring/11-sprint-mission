package com.sprint.mission.discodeit.security.jwt;

import java.time.Instant;
import java.util.UUID;

// 무상태 access 토큰 모델에서는 refresh 토큰만 서버에 보관한다.
public record JwtInformation(
    UUID userId,
    String refreshToken,
    Instant expiration
) {

}
