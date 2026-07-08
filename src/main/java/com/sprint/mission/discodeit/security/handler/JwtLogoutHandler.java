package com.sprint.mission.discodeit.security.handler;

import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Component
public class JwtLogoutHandler implements LogoutHandler {

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
          String refreshToken = cookie.getValue();

          ResponseCookie deleteCookie = ResponseCookie.from(
                  JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME, "")
              .httpOnly(true)
              .path("/api/auth")
              .maxAge(0)
              .build();

          response.addHeader("Set-Cookie", deleteCookie.toString());
        });

  }
}
