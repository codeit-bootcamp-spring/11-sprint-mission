package com.sprint.mission.discodeit.security.jwt;

import java.util.Date;
import java.util.UUID;

public interface JwtRegistry {

  void registerJwtInformation(UUID userId, JwtInformation jwtInformation);

  void invalidateJwtInformationByUserId(UUID userId);

  boolean hasActiveJwtInformationByUserId(UUID userId);

  boolean hasActiveJwtInformationByAccessToken(String accessToken);

  boolean hasActiveJwtInformationByRefreshToken(String refreshToken);

  void rotateJwtInformation(String refreshToken, String newAccessToken,
      String newRefreshToken, Date newAccessTokenExpiration, Date newRefreshTokenExpiration);

  void clearExpiredJwtInformation();

  void invalidateJwtInformationByRefreshToken(String refreshToken);
}