package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.controller.api.AuthApi;
import com.sprint.mission.discodeit.dto.authDto.LoginRequest;
import com.sprint.mission.discodeit.dto.error.ExceptionDto;
import com.sprint.mission.discodeit.dto.userdto.UserDto;
import com.sprint.mission.discodeit.exception.service.DiffPasswordException;
import com.sprint.mission.discodeit.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController implements AuthApi {

  private final AuthService authService;

  @Override
  @PostMapping(value = "login")
  public ResponseEntity<UserDto> login(@RequestBody LoginRequest loginRequest) {
    authService.login(loginRequest);
    return ResponseEntity.status(200).body(authService.login(loginRequest));
  }

  @ExceptionHandler(DiffPasswordException.class)
  public ResponseEntity<ExceptionDto> diffPasswordHandler(DiffPasswordException e,
      HttpServletRequest request) {

    ExceptionDto exceptionDto = ExceptionDto.of(
        HttpStatus.BAD_REQUEST,
        "존재하지 않는 이름 또는 비밀번호 입니다",
        request.getRequestURI()
    );

    return ResponseEntity.status(400).body(exceptionDto);

  }

}
