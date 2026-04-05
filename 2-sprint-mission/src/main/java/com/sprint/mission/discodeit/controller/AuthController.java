package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.AuthDto;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.service.basic.BasicAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final BasicAuthService basicAuthService;

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity<UserDto.Response> login(
            @Valid @RequestBody AuthDto.LoginRequest request
    ) {
        UserDto.Response response = basicAuthService.login(request);
        return ResponseEntity.ok(response);
    }

}