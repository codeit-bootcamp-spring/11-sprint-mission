package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.JwtDto;
import com.sprint.mission.discodeit.dto.JwtInformation;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.exception.auth.RefreshTokenInvalidException;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.security.jwt.JwtRegistry;
import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import com.sprint.mission.discodeit.security.jwt.RefreshTokenCookieFactory;
import com.sprint.mission.discodeit.service.AuthService;
import com.sprint.mission.discodeit.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
@RequiredArgsConstructor
public class AuthController {

  private final UserService userService;
  private final AuthService authService;
  private final JwtTokenProvider jwtTokenProvider;
  private final JwtRegistry jwtRegistry;
  private final RefreshTokenCookieFactory refreshTokenCookieFactory;

  @GetMapping("/csrf-token")
  public ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken) {
    String tokenValue = csrfToken.getToken();
    log.debug("CSRF 토큰 요청: {}", tokenValue);
    return ResponseEntity.status(HttpStatus.NON_AUTHORITATIVE_INFORMATION).build();
  }

  @PutMapping("/role")
  public ResponseEntity<UserDto.Response> updateRole(
      @Valid @RequestBody UserRoleUpdateRequest request) {
    log.info("권한 수정 요청: userId={}, newRole={}", request.userId(), request.newRole());
    UserDto.Response userDto = authService.updateRole(request);

    log.debug("권한 수정 응답: {}", userDto);
    return ResponseEntity.status(HttpStatus.OK).body(userDto);
  }

  @PostMapping("/refresh")
  public ResponseEntity<JwtDto> refresh(
      @CookieValue(name = JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME, required = false)
      String refreshToken,
      HttpServletResponse response
  ) {
    log.debug("토큰 리프레시 요청: {}", refreshToken);
    if (refreshToken == null
        || !jwtTokenProvider.validateRefreshToken(refreshToken)
        || !jwtRegistry.hasActiveJwtInformationByRefreshToken(refreshToken)) {
      throw RefreshTokenInvalidException.invalid();
    }

    UUID userId = jwtTokenProvider.getUserId(refreshToken);
    UserDto.Response userDto = userService.findById(userId);
    DiscodeitUserDetails userDetails = new DiscodeitUserDetails(userDto, null);

    String newAccessToken = jwtTokenProvider.generateAccessToken(userDetails);
    String newRefreshToken = jwtTokenProvider.generateRefreshToken(userDetails);
    Instant expiration = jwtTokenProvider.getExpiration(newRefreshToken);

    JwtInformation newInfo = new JwtInformation(userDto, newAccessToken, newRefreshToken,
        expiration);
    jwtRegistry.rotateJwtInformation(refreshToken, newInfo);

    ResponseCookie refreshCookie = refreshTokenCookieFactory.create(newRefreshToken);
    response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

    return ResponseEntity.ok(new JwtDto(userDto, newAccessToken));
  }
}