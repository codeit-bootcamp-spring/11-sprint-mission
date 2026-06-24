package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.AuthApi;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController implements AuthApi {

  @GetMapping(path = "csrf-token")
  public ResponseEntity<Void> csrfToken(CsrfToken csrfToken) {
    log.debug("CSRF 토큰 발급 요청: token={}", csrfToken.getToken());
    return ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .build();
  }
}
