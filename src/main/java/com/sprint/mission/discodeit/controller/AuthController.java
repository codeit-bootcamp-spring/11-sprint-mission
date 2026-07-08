package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.exception.auth.RefreshTokenInvalidException;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.security.JwtDto;
import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "Auth")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

  private final JwtTokenProvider jwtTokenProvider;
  private final UserDetailsService userDetailsService;
  // private final JwtRegistry jwtRegistry;

  // TODO: 비즈니스 로직은 BasicAuthService로 이동 고려
  @PostMapping("/refresh")
  public ResponseEntity<JwtDto> refresh(
      @CookieValue(name = JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken
  ) {
    if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
      throw new RefreshTokenInvalidException();
    }

    try {
      String username = jwtTokenProvider.getSubject(refreshToken);

      DiscodeitUserDetails userDetails = (DiscodeitUserDetails) userDetailsService.loadUserByUsername(
          username);

      String newAccessToken = jwtTokenProvider.generateAccessToken(userDetails);
      String newRefreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

      ResponseCookie responseCookie = ResponseCookie.from(
              JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME, newRefreshToken)
          .httpOnly(true)
          .path("/api/auth")
          .maxAge(jwtTokenProvider.getRefreshTokenExpiration()
              / 1000) // ResponseCookie의 maxAge()는 초(s) 단위로 받음
          .build();

      JwtDto jwtDto = new JwtDto(userDetails.getUserDto(), newAccessToken);

      return ResponseEntity.ok()
          .header("Set-Cookie", responseCookie.toString())
          .contentType(MediaType.APPLICATION_JSON)
          .body(jwtDto);
    } catch (Exception e) {
      throw new RefreshTokenInvalidException();
    }
  }

  @GetMapping("/csrf-token")
  public ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken) {
    String tokenValue = csrfToken.getToken();
    log.debug("CSRF 토큰 요청: {}", tokenValue);
    return ResponseEntity.status(HttpStatus.NON_AUTHORITATIVE_INFORMATION).build();
  }
}
