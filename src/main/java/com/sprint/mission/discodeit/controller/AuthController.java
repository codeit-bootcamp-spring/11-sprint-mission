package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.AuthApi;
import com.sprint.mission.discodeit.dto.auth.JwtResponse;
import com.sprint.mission.discodeit.dto.auth.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.security.jwt.JwtInformation;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import com.sprint.mission.discodeit.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.time.Duration;
import java.time.Instant;
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
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@RestController
public class AuthController implements AuthApi {

  private final AuthService authService;

  @GetMapping(path = "csrf-token")
  public ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken) {
    log.info("auth csrf-token request");
    log.debug("auth csrf-token response: {}", csrfToken.getToken());
    return ResponseEntity
        .status(HttpStatus.NON_AUTHORITATIVE_INFORMATION)
        .build();
  }

  @PutMapping(path = "role")
  public ResponseEntity<UserResponse> updateRole(
      @Valid @RequestBody UserRoleUpdateRequest request) {
    log.info("auth role request: {}", request);
    UserResponse response = authService.updateRole(request);
    log.debug("auth role request: {}", response);
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(response);
  }

  @PostMapping(path = "refresh")
  public ResponseEntity<JwtResponse> refresh(
      @CookieValue(name = JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME, required = false)
      String refreshToken,
      HttpServletResponse response) {
    JwtInformation jwtInformation = authService.refresh(refreshToken);

    ResponseCookie cookie = ResponseCookie
        .from(JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME, jwtInformation.getRefreshToken())
        .httpOnly(true)
        .sameSite("Lax")
        .path("/")
        .maxAge(Duration.between(Instant.now(), jwtInformation.getExpiration()))
        .build();
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

    return ResponseEntity
        .status(HttpStatus.OK)
        .body(new JwtResponse(jwtInformation.getUserResponse(), jwtInformation.getAccessToken()));
  }
}