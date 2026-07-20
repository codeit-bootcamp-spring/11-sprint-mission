package com.sprint.mission.discodeit.security.jwt;

import com.sprint.mission.discodeit.security.jwt.dto.JwtInformation;
import java.util.Set;
import java.util.UUID;

public interface JwtRegistry {

  void registerJwtInformation(JwtInformation jwtInformation);

  void invalidateJwtInformationByUserId(UUID userId);

  boolean hasActiveJwtInformationByUserId(UUID userId);

  boolean hasActiveJwtInformationByAccessToken(String accessToken);

  boolean hasActiveJwtInformationByRefreshToken(String refreshToken);

  JwtInformation rotateJwtInformation(String oldRefreshToken, JwtInformation newJwtInformation);

  Set<UUID> getActiveUserIds();

  void clearExpiredJwtInformation();
}
