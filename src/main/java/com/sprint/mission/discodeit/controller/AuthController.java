package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.AuthApi;
import com.sprint.mission.discodeit.dto.data.JwtDto;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.RoleUpdateRequest;
import com.sprint.mission.discodeit.exception.jwt.JwtException;
import com.sprint.mission.discodeit.exception.jwt.RefreshTokenInvalidException;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.security.DiscodeitUserDetailsService;
import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import com.sprint.mission.discodeit.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController  {

  private final AuthService authService;
  private final JwtTokenProvider jwtTokenProvider;
  private final DiscodeitUserDetailsService userDetailsService;

  @PostMapping("refresh")
  public ResponseEntity<JwtDto> refresh(
      @CookieValue(name = JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken,
      HttpServletResponse response
  ) {
    if (refreshToken == null) {
      throw new RefreshTokenInvalidException();
    }
    try {
      jwtTokenProvider.validateToken(refreshToken);
    } catch (JwtException e) {
      throw new RefreshTokenInvalidException();
    }

    String userId = jwtTokenProvider.getSubject(refreshToken);
    DiscodeitUserDetails userDetails = userDetailsService.loadUserById(UUID.fromString(userId));

    String newAccessToken = jwtTokenProvider.generateAccessToken(userDetails);
    String newRefreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

    ResponseCookie refreshTokenCookie = ResponseCookie
        .from(JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME, newRefreshToken)
        .httpOnly(true)
        .path("/")
        .sameSite("Strict")
        .build();
    response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

    return ResponseEntity
        .status(HttpStatus.OK)
        .body(new JwtDto(userDetails.getUserDto(), newAccessToken));
  }

  @PutMapping("role")
  public ResponseEntity<UserDto> updateRole(@RequestBody RoleUpdateRequest request) {
    log.info("권한 수정 요청");
    UserDto userDto = authService.updateRole(request);

    return ResponseEntity
        .status(HttpStatus.OK)
        .body(userDto);
  }
}
