package com.sprint.mission.discodeit.security.jwt.model;

import java.time.Instant;
import java.util.UUID;

public record JwtInformation(
    UUID userId, // 어떤 사용자의 토큰인지 식별
    String accessToken, // 발급된 Access Token
    String refreshToken, // 발급된 Refresh Token
    Instant expiration // 만료 시각(유효기간이 긴 토큰, 여기서는 Refresh Token)
) {

  // 토큰 회전
  // record는 불변성을 띄기 때문에 수정이 아닌 삭제 → "생성" 방식으로 갱신 개념
  public JwtInformation rotate(String newAccessToken, String newRefreshToken) {
    return new JwtInformation(userId, newAccessToken, newRefreshToken, expiration);
  }

  public String getAccessToken() {
    return accessToken;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

}
