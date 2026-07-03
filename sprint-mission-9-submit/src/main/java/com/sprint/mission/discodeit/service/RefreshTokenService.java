package com.sprint.mission.discodeit.service;

import java.time.Instant;
import java.util.UUID;

public interface RefreshTokenService {
  void saveRefreshToken(String token, UUID userId, Instant expiresAt);

  UUID validateAndGetUserId(String token);

  void rotateRefreshToken(String oldToken, String newToken, UUID userId, Instant newExpiresAt);

  void invalidate(String token);
}
