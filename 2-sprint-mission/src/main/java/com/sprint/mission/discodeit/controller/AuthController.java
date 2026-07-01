package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.AuthService;
import com.sprint.mission.discodeit.service.UserService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
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

  @GetMapping("/csrf-token")
  public ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken) {
    String tokenValue = csrfToken.getToken();
    log.debug("CSRF 토큰 요청: {}", tokenValue);
    return ResponseEntity.status(HttpStatus.NON_AUTHORITATIVE_INFORMATION).build();
  }

  @GetMapping("/me")
  public ResponseEntity<UserDto.Response> getMe(
      @AuthenticationPrincipal DiscodeitUserDetails userDetails) {
    log.debug("내 정보 조회 요청: username={}", userDetails.getUsername());
    UUID userId = userDetails.getUserDto().id();
    UserDto.Response userDto = userService.findById(userId);
    return ResponseEntity.status(HttpStatus.OK).body(userDto);
  }

  @PutMapping("/role")
  public ResponseEntity<UserDto.Response> updateRole(
      @Valid @RequestBody UserRoleUpdateRequest request) {
    log.info("권한 수정 요청: userId={}, newRole={}", request.userId(), request.newRole());
    UserDto.Response userDto = authService.updateRole(request);

    log.debug("권한 수정 응답: {}", userDto);
    return ResponseEntity.status(HttpStatus.OK).body(userDto);
  }
}