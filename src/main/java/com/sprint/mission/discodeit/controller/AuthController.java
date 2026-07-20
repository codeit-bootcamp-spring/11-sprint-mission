package com.sprint.mission.discodeit.controller;

import static com.sprint.mission.discodeit.security.jwt.provider.JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME;

import com.sprint.mission.discodeit.constant.EndPoints;
import com.sprint.mission.discodeit.controller.api.AuthApi;
import com.sprint.mission.discodeit.dto.request.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.security.jwt.dto.JwtDto;
import com.sprint.mission.discodeit.security.jwt.dto.RefreshTokenResult;
import com.sprint.mission.discodeit.security.jwt.properties.JwtProperties;
import com.sprint.mission.discodeit.security.jwt.provider.JwtTokenProvider;
import com.sprint.mission.discodeit.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
@RestController // Json 반환을 위해 @Controller 대신 @ResponseBody를 포함한 @RestController 사용
@RequestMapping(EndPoints.AUTH)
@RequiredArgsConstructor
public class AuthController implements AuthApi {

  private final AuthService authService;
  private final JwtProperties jwtProperties;
  private final JwtTokenProvider jwtTokenProvider;

  @Override
  @GetMapping("/csrf-token")
  public ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken) {
    String tokenValue = csrfToken.getToken();
    log.debug("CSRF 토큰 요청: {}", tokenValue);

    return ResponseEntity.status(HttpStatus.NON_AUTHORITATIVE_INFORMATION).build();
  }

  @Override
  @PutMapping("/role")
  public ResponseEntity<UserDto> updateRole(
      @RequestBody UserRoleUpdateRequest dto
  ) {
    UserDto user = authService.updateRole(dto);

    return ResponseEntity.status(HttpStatus.OK).body(user);
  }

  @Override
  @PostMapping("/refresh")
  public ResponseEntity<JwtDto> refresh(
      @CookieValue(name = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken,
      HttpServletResponse response
  ) {
    RefreshTokenResult result = authService.refresh(refreshToken);

    // Refresh Token 쿠키 교체
    Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, result.newRefreshToken());

    // JavaScript 접근 차단
    cookie.setHttpOnly(true);

    // HTTPS에서만 접근 가능하도록 설정
    cookie.setSecure(true);

    // 내 도메인 내의 모든 URI에 쿠키 적용
    cookie.setPath("/");

    // 쿠키 유효기간을 30일로 설정(setMaxAge()는 초 단위이기 때문에 60을 곱하여 초 단위로 변환)
    cookie.setMaxAge(jwtProperties.getRefreshTokenExpiration() * 60);

    response.addCookie(cookie);

    // Access Token 응답
    JwtDto jwtDto = result.jwtDto();

    return ResponseEntity.status(HttpStatus.OK).body(jwtDto);
  }
}