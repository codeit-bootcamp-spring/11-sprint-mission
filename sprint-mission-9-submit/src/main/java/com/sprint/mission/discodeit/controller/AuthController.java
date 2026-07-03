package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.AuthApi;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.RoleUpdateRequest;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.AuthService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.sprint.mission.discodeit.dto.response.JwtDto;
import com.sprint.mission.discodeit.exception.auth.RefreshTokenInvalidException;
import com.sprint.mission.discodeit.security.DiscodeitUserDetailsService;
import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import com.sprint.mission.discodeit.service.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.time.Instant;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController implements AuthApi {

  private final AuthService authService;
  private final JwtTokenProvider jwtTokenProvider;
  private final RefreshTokenService refreshTokenService;
  private final DiscodeitUserDetailsService userDetailsService;

  @GetMapping("csrf-token")
  public ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken) {
    log.debug("CSRF 토큰 요청");
    log.trace("CSRF 토큰: {}", csrfToken.getToken());
    return ResponseEntity
        .status(HttpStatus.NO_CONTENT)
        .build();
  }

  @PutMapping("role")
  public ResponseEntity<UserDto> updateRole(@RequestBody RoleUpdateRequest request) {
    log.info("권한 수정 요청");
    UserDto userDto = authService.updateRole(request);

    return ResponseEntity
        .status(HttpStatus.OK)
        .body(userDto);
  }

  @PostMapping("refresh")
  public ResponseEntity<JwtDto> refresh(
      @CookieValue(name = JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME, required = false)
      String refreshToken,
      HttpServletRequest request,
      HttpServletResponse response
  ) {
    if (refreshToken == null || refreshToken.isBlank()) {
      throw new RefreshTokenInvalidException();
    }

    if (!jwtTokenProvider.validateToken(refreshToken)) {
      throw new RefreshTokenInvalidException();
    }

    UUID userId = refreshTokenService.validateAndGetUserId(refreshToken);

    DiscodeitUserDetails userDetails = userDetailsService.loadUserById(userId);

    String newAccessToken = jwtTokenProvider.reissueAccessToken(refreshToken, userDetails);
    String newRefreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

    refreshTokenService.rotateRefreshToken(
        refreshToken,
        newRefreshToken,
        userId,
        jwtTokenProvider.getExpiration(newRefreshToken)
    );

    Cookie refreshTokenCookie = new Cookie(
        JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME,
        newRefreshToken
    );
    refreshTokenCookie.setHttpOnly(true);
    refreshTokenCookie.setPath("/");
    refreshTokenCookie.setSecure(request.isSecure());

    int maxAge = (int) Duration.between(
        Instant.now(),
        jwtTokenProvider.getExpiration(newRefreshToken)
    ).getSeconds();

    refreshTokenCookie.setMaxAge(maxAge);
    response.addCookie(refreshTokenCookie);

    return ResponseEntity
        .status(HttpStatus.OK)
        .body(new JwtDto(newAccessToken));
  }
}
