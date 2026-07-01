package com.sprint.mission.discodeit.security.handler;

import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtLogoutHandler implements LogoutHandler {

  private final JwtTokenProvider jwtTokenProvider;
  // TODO: JwtRegistry 구현 후 주입받아, 로그아웃 시 해당 refresh token을 레지스트리에서 삭제하도록 반영

  @Override
  public void logout(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return;
    }
    Arrays.stream(cookies)
        .filter(cookie -> cookie.getName().equals(JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME))
        .findFirst()
        .ifPresent(cookie -> {
          // TODO: JwtRegistry로 cookie.getValue()에 해당하는 JwtInformation 무효화

          ResponseCookie expiredCookie = jwtTokenProvider.expireRefreshTokenCookie();
          response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie.toString());
        });
  }
}
