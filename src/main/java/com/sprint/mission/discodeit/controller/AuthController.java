package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.sprint.mission.discodeit.dto.request.UserRoleUpdateRequest;


@Slf4j
@RequiredArgsConstructor  // 추가
@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final UserService userService;  // 추가

  @GetMapping("csrf-token")
  public ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken) {
    String tokenValue = csrfToken.getToken();
    log.debug("CSRF 토큰 요청: {}", tokenValue);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("me")
  public ResponseEntity<UserDto> getMe(
      @AuthenticationPrincipal DiscodeitUserDetails userDetails) {
    log.debug("현재 사용자 조회: {}", userDetails.getUserDto());
    return ResponseEntity.ok(userDetails.getUserDto());
  }


  @PutMapping("role")
  public ResponseEntity<UserDto> updateRole(@RequestBody UserRoleUpdateRequest request) {
    return ResponseEntity.ok(userService.updateRole(request));
  }
}