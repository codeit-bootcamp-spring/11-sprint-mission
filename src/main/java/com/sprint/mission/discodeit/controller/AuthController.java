package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.AuthApi;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController implements AuthApi {

  private final UserMapper userMapper;

  @GetMapping("me")
  public ResponseEntity<UserDto> me(
      @AuthenticationPrincipal DiscodeitUserDetails userDetails
  ) {
    // 현재 인증된 사용자 정보를 UserDto로 변환해서 반환함
    UserDto userDto = userMapper.toDto(userDetails.getUser());

    return ResponseEntity
        .status(HttpStatus.OK)
        .body(userDto);
  }

  @GetMapping("csrf-token")
  public ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken) {
    // CsrfToken 파라미터를 선언하면 Spring Security가 토큰을 주입함
    // 토큰을 읽는 순간 CookieCsrfTokenRepository가 XSRF-TOKEN 쿠키를 응답에 포함함
    log.debug("CSRF 토큰 발급 요청: {}", csrfToken.getToken());

    return ResponseEntity
        .status(HttpStatus.NON_AUTHORITATIVE_INFORMATION)
        .build();
  }
}
