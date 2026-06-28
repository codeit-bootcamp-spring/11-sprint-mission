package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.AuthApi;
import com.sprint.mission.discodeit.dto.auth.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
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
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@RestController
public class AuthController implements AuthApi {

  private final AuthService authService;
  private final UserService userService;

  @GetMapping(path = "csrf-token")
  public ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken) {
    log.info("auth csrf-token request");
    log.debug("auth csrf-token response: {}", csrfToken.getToken());
    return ResponseEntity
        .status(HttpStatus.NON_AUTHORITATIVE_INFORMATION)
        .build();
  }

  @GetMapping(path = "me")
  public ResponseEntity<UserResponse> me(
      @AuthenticationPrincipal DiscodeitUserDetails userDetails) {
    UUID userId = userDetails.getUser().id();
    log.info("auth me request: userId={}", userId);
    UserResponse response = userService.findById(userId);
    log.debug("auth me response: {}", response);
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(response);
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
}
