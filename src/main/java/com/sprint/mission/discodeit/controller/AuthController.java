package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.AuthApi;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.RoleUpdateRequest;
import com.sprint.mission.discodeit.dto.data.JwtDto;
import com.sprint.mission.discodeit.dto.data.JwtRefreshResult;
import com.sprint.mission.discodeit.security.JwtTokenProvider;
import com.sprint.mission.discodeit.service.JwtAuthService;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletResponse;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController implements AuthApi {

  private final UserService userService;
  private final JwtAuthService jwtAuthService;
  private final JwtTokenProvider jwtTokenProvider;

  @GetMapping("csrf-token")
  public ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken) {
    // CsrfToken 파라미터를 선언하면 Spring Security가 토큰을 주입함
    // 토큰을 읽는 순간 CookieCsrfTokenRepository가 XSRF-TOKEN 쿠키를 응답에 포함함
    log.debug("CSRF 토큰 발급 요청: {}", csrfToken.getToken());

    return ResponseEntity
        .status(HttpStatus.NON_AUTHORITATIVE_INFORMATION)
        .build();
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("role")
  public ResponseEntity<UserDto> updateRole(
      @RequestBody @Valid RoleUpdateRequest request
  ) {
    // ADMIN 권한을 가진 사용자만 다른 사용자의 역할을 변경할 수 있음
    UserDto userDto = userService.updateRole(request.userId(), request.newRole());

    return ResponseEntity
        .status(HttpStatus.OK)
        .body(userDto);
  }

  @PostMapping("refresh")
  public ResponseEntity<JwtDto> refresh(
      @CookieValue(name = JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME, required = false)
      String refreshToken,
      HttpServletResponse response
  ) {
    // refresh token으로 새 access token과 새 refresh token을 발급함
    JwtRefreshResult result = jwtAuthService.refresh(refreshToken);

    // 새 refresh token을 쿠키로 다시 저장함
    response.addCookie(jwtTokenProvider.createRefreshTokenCookie(result.refreshToken()));

    return ResponseEntity
        .status(HttpStatus.OK)
        .body(result.jwtDto());
  }
}
