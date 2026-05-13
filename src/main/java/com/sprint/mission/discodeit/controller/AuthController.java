package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.controller.api.AuthApi;
import com.sprint.mission.discodeit.dto.authDto.LoginRequest;
import com.sprint.mission.discodeit.dto.userdto.UserDto;
import com.sprint.mission.discodeit.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController implements AuthApi {

  private final AuthService authService;

  @Override
  @PostMapping(value = "login")
  public ResponseEntity<UserDto> login(@Valid @RequestBody LoginRequest loginRequest) {
    authService.login(loginRequest);
    return ResponseEntity.status(200).body(authService.login(loginRequest));
  }


}
