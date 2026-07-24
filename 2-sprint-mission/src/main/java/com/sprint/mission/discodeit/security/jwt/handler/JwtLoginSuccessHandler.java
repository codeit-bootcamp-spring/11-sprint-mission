package com.sprint.mission.discodeit.security.jwt.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.JwtDto;
import com.sprint.mission.discodeit.dto.JwtInformation;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.security.jwt.JwtRegistry;
import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import com.sprint.mission.discodeit.security.jwt.RefreshTokenCookieFactory;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtLoginSuccessHandler implements AuthenticationSuccessHandler {

  private final JwtTokenProvider jwtTokenProvider;
  private final ObjectMapper objectMapper;
  private final JwtRegistry jwtRegistry;
  private final RefreshTokenCookieFactory refreshTokenCookieFactory;
  private final CacheManager cacheManager;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {
    if (!(authentication.getPrincipal() instanceof DiscodeitUserDetails userDetails)) {
      log.warn("로그인 처리 중 Principal 타입 오류: {}",
          authentication.getPrincipal().getClass().getSimpleName());
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
    String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

    Instant expiration = jwtTokenProvider.getExpiration(refreshToken);
    jwtRegistry.registerJwtInformation(
        new JwtInformation(userDetails.getUserDto(), accessToken, refreshToken, expiration));
    evictUsersCache();

    ResponseCookie refreshCookie = refreshTokenCookieFactory.create(refreshToken);
    response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

    response.setStatus(HttpServletResponse.SC_OK);
    response.setCharacterEncoding("UTF-8");
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    JwtDto jwtDto = new JwtDto(userDetails.getUserDto(), accessToken);
    response.getWriter().write(objectMapper.writeValueAsString(jwtDto));

    log.info("로그인 성공, 토큰 발급: username={}", userDetails.getUsername());
  }

  private void evictUsersCache() {
    Cache cache = cacheManager.getCache("users");
    if (cache != null) {
      cache.clear();
    }
  }
}