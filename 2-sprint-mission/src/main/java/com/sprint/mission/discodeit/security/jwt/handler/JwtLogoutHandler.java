package com.sprint.mission.discodeit.security.jwt.handler;

import com.sprint.mission.discodeit.event.UserUpdatedEvent;
import com.sprint.mission.discodeit.security.jwt.JwtRegistry;
import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import com.sprint.mission.discodeit.security.jwt.RefreshTokenCookieFactory;
import com.sprint.mission.discodeit.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
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
  private final JwtRegistry jwtRegistry;
  private final RefreshTokenCookieFactory refreshTokenCookieFactory;
  private final CacheManager cacheManager;
  private final UserService userService;
  private final ApplicationEventPublisher eventPublisher;

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
            UUID userId = jwtTokenProvider.getUserId(refreshToken);
            jwtRegistry.invalidateJwtInformationByUserId(userId);
            evictUsersCache();

            publishUserUpdated(userId);
          }
          ResponseCookie expiredCookie = refreshTokenCookieFactory.expired();
          response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie.toString());

          log.info("로그아웃: REFRESH_TOKEN 쿠키 삭제");
        });
  }

  private void evictUsersCache() {
    Cache cache = cacheManager.getCache("users");
    if (cache != null) {
      cache.clear();
    }
  }
  
  private void publishUserUpdated(UUID userId) {
    try {
      eventPublisher.publishEvent(new UserUpdatedEvent(userService.findById(userId)));
    } catch (Exception e) {
      log.warn("온라인 상태 변경 이벤트 발행 실패: userId={}", userId, e);
    }
  }
}