package com.sprint.mission.discodeit.security.jwt;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtLogoutHandler implements LogoutHandler {

  private final JwtRegistry jwtRegistry;

  @Override
  public void logout(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) {
    if (request.getCookies() == null) {
      return;
    }

    Arrays.stream(request.getCookies())
        .filter(cookie -> cookie.getName().equals(JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME))
        .findFirst()
        .ifPresent(cookie -> {
          jwtRegistry.invalidateJwtInformationByRefreshToken(cookie.getValue());

          Cookie expiredCookie = new Cookie(JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME, null);
          expiredCookie.setHttpOnly(true);
          expiredCookie.setPath("/");
          expiredCookie.setMaxAge(0);
          response.addCookie(expiredCookie);
          log.debug("리프레시 토큰 쿠키 삭제 완료");
        });
  }
}