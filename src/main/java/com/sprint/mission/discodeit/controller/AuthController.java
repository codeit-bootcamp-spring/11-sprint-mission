package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.auth.AuthLoginRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@RestController
public class AuthController {
    private final AuthService authService;

    @RequestMapping(
            path = "login",
            method = RequestMethod.POST
    )
    public ResponseEntity<UserResponse> login(
            @RequestBody AuthLoginRequest authLoginRequest
    ) {
        UserResponse loginUSer = this.authService.login(authLoginRequest);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(loginUSer);
    }
}
