package com.sprint.mission.discodeit.security.jwt;

import com.sprint.mission.discodeit.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtLogoutHandler implements LogoutHandler {

  private final JwtTokenProvider jwtTokenProvider;
  private final AuthService authService;

  @Override
  public void logout(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) {
    String refreshToken = extractRefreshToken(request);

    if (refreshToken != null) {
      try {
        UUID userId = jwtTokenProvider.getUserId(refreshToken);
        authService.saveRefreshToken(userId, null);
      } catch (RuntimeException e) {
        log.debug("로그아웃 처리 중 리프레시 토큰 무효화에 실패했습니다 (무시하고 진행): {}", e.getMessage());
      }
    }

    ResponseCookie expiredCookie = jwtTokenProvider.expireRefreshTokenCookie();
    response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie.toString());
  }

  private String extractRefreshToken(HttpServletRequest request) {
    if (request.getCookies() == null) {
      return null;
    }
    return Arrays.stream(request.getCookies())
        .filter(cookie -> JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName()))
        .map(Cookie::getValue)
        .findFirst()
        .orElse(null);
  }
}
