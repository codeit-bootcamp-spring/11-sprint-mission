package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.response.JwtDto;
import com.sprint.mission.discodeit.exception.AuthenticationException;
import com.sprint.mission.discodeit.security.jwt.JwtRegistry;
import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import com.sprint.mission.discodeit.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final JwtTokenProvider jwtTokenProvider;
  private final JwtRegistry jwtRegistry;
  private final UserService userService;

  @PostMapping("/refresh")
  public ResponseEntity<JwtDto> refresh(HttpServletRequest request,
      HttpServletResponse response) {
    String refreshToken = extractRefreshToken(request);

    if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)
        || !jwtRegistry.hasActiveJwtInformationByRefreshToken(refreshToken)) {
      throw new AuthenticationException("유효하지 않은 리프레시 토큰입니다.");
    }

    UUID userId = jwtTokenProvider.getUserId(refreshToken);
    String username = jwtTokenProvider.getUsername(refreshToken);
    String role = jwtTokenProvider.getRole(refreshToken);

    String newAccessToken = jwtTokenProvider.generateAccessToken(userId, username, role);
    String newRefreshToken = jwtTokenProvider.generateRefreshToken(userId, username, role);

    // 토큰 로테이션
    jwtRegistry.rotateJwtInformation(refreshToken, newAccessToken, newRefreshToken,
        jwtTokenProvider.getExpirationTime(newAccessToken),
        jwtTokenProvider.getExpirationTime(newRefreshToken));

    // 새 리프레시 토큰 쿠키 갱신
    Cookie refreshCookie = new Cookie(JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME, newRefreshToken);
    refreshCookie.setHttpOnly(true);
    refreshCookie.setPath("/");
    refreshCookie.setMaxAge((int) (jwtTokenProvider.getRefreshTokenExpiration() / 1000));
    response.addCookie(refreshCookie);

    return ResponseEntity.ok(JwtDto.of(newAccessToken));
  }

  private String extractRefreshToken(HttpServletRequest request) {
    if (request.getCookies() == null) {
      return null;
    }
    return Arrays.stream(request.getCookies())
        .filter(cookie -> cookie.getName().equals(JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME))
        .findFirst()
        .map(Cookie::getValue)
        .orElse(null);
  }
}