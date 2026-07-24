package com.sprint.mission.discodeit.security.jwt;

import java.time.Duration;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenCookieFactory {

  private final long refreshTokenExpirationMs;
  private final boolean secure;

  public RefreshTokenCookieFactory(JwtTokenProvider jwtTokenProvider) {
    this.refreshTokenExpirationMs = jwtTokenProvider.getRefreshTokenExpirationMs();
    this.secure = false;   // 로컬 HTTP용, 운영(HTTPS)에선 true
  }

  // 발급/재발급용: 토큰을 담은 쿠키
  public ResponseCookie create(String refreshToken) {
    return build(refreshToken, Duration.ofMillis(refreshTokenExpirationMs));
  }

  // 로그아웃용: 빈 값 + 즉시 만료 쿠키
  public ResponseCookie expired() {
    return build("", Duration.ZERO);
  }

  private ResponseCookie build(String value, Duration maxAge) {
    return ResponseCookie
        .from(JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME, value)
        .httpOnly(true)
        .secure(secure)
        .path("/")
        .maxAge(maxAge)
        .sameSite("Lax")
        .build();
  }
}