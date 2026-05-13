package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.constant.EndPoints;
import com.sprint.mission.discodeit.controller.api.AuthApi;
import com.sprint.mission.discodeit.dto.request.LoginRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController // Json 반환을 위해 @Controller 대신 @ResponseBody를 포함한 @RestController 사용
@RequestMapping(EndPoints.AUTH)
@RequiredArgsConstructor
public class AuthController implements AuthApi {

  private final AuthService authService;

  @Override
  @PostMapping("/login") // 보안을 위해 GET이 아닌 POST 사용
  public ResponseEntity<UserDto> login(@Valid @RequestBody LoginRequest dto) {
    return ResponseEntity.ok(authService.login(dto));
  }
}