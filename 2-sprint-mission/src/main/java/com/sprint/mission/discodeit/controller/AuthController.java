package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.AuthDto;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService basicAuthService;

  @PostMapping("/login")
  public ResponseEntity<UserDto.Response> login(
      @Valid @RequestBody AuthDto.LoginRequest request
  ) {
    log.info("로그인 요청: username={}", request.username());
    UserDto.Response response = basicAuthService.login(request);

    log.debug("로그인 응답: {}", response);
    return ResponseEntity.ok(response);
  }

}