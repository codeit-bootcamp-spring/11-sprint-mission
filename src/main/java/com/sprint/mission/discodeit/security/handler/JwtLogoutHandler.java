package com.sprint.mission.discodeit.security.handler;

import com.sprint.mission.discodeit.security.jwt.JwtRegistry;
import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.UUID;
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
  private final JwtRegistry jwtRegistry;

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
          UUID userId = jwtTokenProvider.getUserId(cookie.getValue());
          jwtRegistry.invalidateJwtInformationByUserId(userId);

          ResponseCookie expiredCookie = jwtTokenProvider.expireRefreshTokenCookie();
          response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie.toString());
        });
  }
}
