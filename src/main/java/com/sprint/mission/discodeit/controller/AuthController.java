package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.auth.DiscodeitUserDetails;
import com.sprint.mission.discodeit.controller.api.AuthApi;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController implements AuthApi {

  private final AuthService authService;

  // 동작
  // - 클라이언트가 API를 호출하면, 파라미터로 주입된 CsrfToken의 getToken()이 실행
  // - SpaCsrfTokenRequestHandler를 거쳐 응답 쿠리(XSRF-TOKEN)에 토큰이 심어짐
  @GetMapping("/csrf-token")
  public ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken) {
    String tokenValue = csrfToken.getToken();
    log.debug("CSRF 토큰 발급 완료: {}", tokenValue);
    return ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .build();
  }

  @GetMapping("/me")
  public ResponseEntity<UserDto> getCurrentUser(@AuthenticationPrincipal DiscodeitUserDetails userDetails) {

    if (userDetails == null) {
      log.warn("인증되지 않은 사용자의 /me 요청");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    return ResponseEntity
            .status(HttpStatus.OK)
            .body(userDetails.getUserDto());
  }

  @PutMapping("/role")
  public ResponseEntity<UserDto> updateUserRole(@Valid @RequestBody UserRoleUpdateRequest request) {
    UserDto userDto = authService.updateUserRole(request);
    return ResponseEntity
            .status(HttpStatus.OK)
            .body(userDto);
  }
}
