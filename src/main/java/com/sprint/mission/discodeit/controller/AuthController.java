package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "Auth")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

  @GetMapping("/me")
  public ResponseEntity<UserDto> getMe(@AuthenticationPrincipal DiscodeitUserDetails userDetails) {
    UserDto userDto = userDetails.getUserDto();
    return ResponseEntity.ok(userDto);
  }

  @GetMapping("/csrf-token")
  public ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken) {
    String tokenValue = csrfToken.getToken();
    log.debug("CSRF 토큰 요청: {}", tokenValue);
    return ResponseEntity.status(HttpStatus.NON_AUTHORITATIVE_INFORMATION).build();
  }
}
