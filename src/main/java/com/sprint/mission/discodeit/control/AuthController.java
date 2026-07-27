package com.sprint.mission.discodeit.control;

import com.sprint.mission.discodeit.dto.auth.JwtDto;
import com.sprint.mission.discodeit.dto.user.RoleUpdateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import com.sprint.mission.discodeit.security.jwt.dto.JwtSessionResult;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.jwt.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
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
@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final UserService userService;
  private final JwtTokenProvider jwtTokenProvider;
  private final JwtService jwtService;

  public AuthController(UserService userService, JwtTokenProvider jwtTokenProvider,
      JwtService jwtService) {
    this.userService = userService;
    this.jwtTokenProvider = jwtTokenProvider;
    this.jwtService = jwtService;
  }

  @PostMapping("/refresh")
  public ResponseEntity<JwtDto> refresh(
      @CookieValue(name = JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken,
      HttpServletResponse response) {
    JwtSessionResult result = jwtService.refreshJwtSession(refreshToken);

    ResponseCookie responseTokenCookie = jwtTokenProvider.createRefreshTokenCookie(
        result.refreshToken());
    response.addHeader(HttpHeaders.SET_COOKIE, responseTokenCookie.toString());

    JwtDto jwtDto = new JwtDto(result.userDto(), result.accessToken());

    return ResponseEntity.ok(jwtDto);
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(
      @CookieValue(name = JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken,
      HttpServletResponse response) {
    jwtService.logoutJwtSession(refreshToken);

    ResponseCookie expiredCookie = jwtTokenProvider.expireRefreshTokenCookie();
    response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie.toString());

    return ResponseEntity.noContent().build();
  }

  @GetMapping("/csrf-token")
  public ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken) {
    String tokenValue = csrfToken.getToken();
    log.debug("CSRF 토큰 요청: {}", tokenValue);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/role")
  public ResponseEntity<UserDto> updateRole(@RequestBody RoleUpdateRequest request) {
    UserDto userDto = userService.updateRole(request);
    log.info("권한 변경 성공 - userId: {}, newRole: {}", request.userId(), request.newRole());
    return ResponseEntity.ok(userDto);
  }
}
