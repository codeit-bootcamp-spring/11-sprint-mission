package com.sprint.mission.discodeit.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtLogoutHandler implements LogoutHandler {

  private final JwtRegistry jwtRegistry;

  @Override
  public void logout(
      HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication
  ) {
    // 로그아웃 요청은 인증 정보가 없을 수도 있으므로 refresh token 쿠키 기준으로 처리함
    Cookie[] cookies = request.getCookies();

    if (cookies == null) {
      return;
    }

    Arrays.stream(cookies)
        .filter(cookie -> JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName()))
        .findFirst()
        .ifPresent(cookie ->
            // refresh token과 연결된 JWT 정보를 registry에서 제거함
            jwtRegistry.invalidateJwtInformationByRefreshToken(cookie.getValue())
        );
  }
}