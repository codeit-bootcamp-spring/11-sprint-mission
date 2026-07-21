package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.security.JwtDto;
import com.sprint.mission.discodeit.security.TokenRefreshResultDto;
import com.sprint.mission.discodeit.security.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import com.sprint.mission.discodeit.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "Auth")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;
  private final JwtTokenProvider jwtTokenProvider;

  @PutMapping("/role")
  public ResponseEntity<UserDto> updateRole(
      @RequestBody UserRoleUpdateRequest request
  ) {
    UserDto userDto = authService.updateRole(request);
    return ResponseEntity.ok(userDto);
  }

  @PostMapping("/refresh")
  public ResponseEntity<JwtDto> refresh(
      @CookieValue(name = JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken
  ) {

    TokenRefreshResultDto refreshResultDto = authService.refreshToken(refreshToken);

    ResponseCookie responseCookie = ResponseCookie.from(
            JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME, refreshResultDto.newRefreshToken())
        .httpOnly(true)
        .path("/api/auth")
        .maxAge(jwtTokenProvider.getRefreshTokenExpiration()
            / 1000) // ResponseCookie의 maxAge()는 초(s) 단위로 받음
        .build();

    return ResponseEntity.ok()
        .header("Set-Cookie", responseCookie.toString())
        .contentType(MediaType.APPLICATION_JSON)
        .body(refreshResultDto.jwtDto());
  }

  @GetMapping("/csrf-token")
  public ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken) {
    String tokenValue = csrfToken.getToken();
    log.debug("CSRF 토큰 요청: {}", tokenValue);
    return ResponseEntity.noContent().build();
  }
}
