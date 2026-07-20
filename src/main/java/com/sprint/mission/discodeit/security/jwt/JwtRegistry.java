package com.sprint.mission.discodeit.security.jwt;

import java.util.UUID;

public interface JwtRegistry {

  void registerJwtInformation(JwtInformation jwtInformation);

  void invalidateJwtInformationByUserId(UUID userId);

  void invalidateJwtInformationByRefreshToken(String refreshToken);

  boolean hasActiveJwtInformationByUserId(UUID userId);

  boolean hasActiveJwtInformationByAccessToken(String accessToken);

  boolean hasActiveJwtInformationByRefreshToken(String refreshToken);

  JwtInformation rotateJwtInformation(String oldRefreshToken, JwtInformation newJwtInformation);

  void clearExpiredJwtInformation();
}
