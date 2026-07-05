package com.sprint.mission.discodeit.dto.auth;

import com.sprint.mission.discodeit.dto.user.UserResponse;
import java.time.Instant;

public record TokenRefreshResult(
    UserResponse userResponse,
    String accessToken,
    String refreshToken,
    Instant refreshTokenExpiration
) {

}