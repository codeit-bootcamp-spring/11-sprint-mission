package com.sprint.mission.discodeit.security.jwt;

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

    if (request.getCookies() == null) {
      return;
    }

    Arrays.stream(request.getCookies())
        .filter(cookie -> cookie.getName().equals(JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME))
        .findFirst()
        .ifPresent(cookie -> {

          String refreshToken = cookie.getValue();

          try {
            String subject = jwtTokenProvider.getSubject(refreshToken);
            UUID userId = UUID.fromString(subject);
            jwtRegistry.invalidateJwtInformationByUserId(userId);
          } catch (Exception e) {
            //예외 나도 삼키고 쿠키 삭제 진행
          }

          ResponseCookie deleteCookie = ResponseCookie.from(
                  JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME, "")
              .httpOnly(true)
              .secure(false)        // 로컬 http면 false
              .path("/")
              .maxAge(0)           // 즉시 삭제
              .sameSite("Strict")
              .build();

          response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
        });

  }
}