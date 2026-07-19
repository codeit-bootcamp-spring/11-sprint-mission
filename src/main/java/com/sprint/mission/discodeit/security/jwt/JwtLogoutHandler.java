package com.sprint.mission.discodeit.security.jwt;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtLogoutHandler implements LogoutHandler {

  private final JwtTokenProvider jwtTokenProvider;
  private final JwtRegistry jwtRegistry;
  private final CacheManager cacheManager;

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
          if (jwtTokenProvider.validateRefreshToken(refreshToken)) {
            UUID userId = UUID.fromString(jwtTokenProvider.getSubject(refreshToken));
            jwtRegistry.invalidateJwtInformationByUserId(userId);
          }

          ResponseCookie expired = ResponseCookie
              .from(JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME, "")
              .httpOnly(true)
              .sameSite("Lax")
              .path("/")
              .maxAge(Duration.ZERO)
              .build();
          response.addHeader(HttpHeaders.SET_COOKIE, expired.toString());

          Optional.ofNullable(cacheManager.getCache("users")).ifPresent(Cache::clear);

          log.info("auth logout: refreshToken={}", refreshToken);
        });
  }
}