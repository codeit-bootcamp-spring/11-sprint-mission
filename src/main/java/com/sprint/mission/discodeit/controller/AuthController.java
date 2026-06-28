package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.AuthApi;
import com.sprint.mission.discodeit.dto.auth.LoginRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@RestController
public class AuthController implements AuthApi {

    private final AuthService authService;

    @GetMapping("csrf-token")
    public ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken) {
        log.info("auth csrf-token request");
        log.debug("auth csrf-token response: {}", csrfToken.getToken());
        return ResponseEntity
                .status(HttpStatus.NON_AUTHORITATIVE_INFORMATION)
                .build();
    }

    @PostMapping(path = "login")
    public ResponseEntity<UserResponse> login(
            @Valid @RequestBody LoginRequest loginRequest) {
        log.info("auth login request: username={}", loginRequest.username());
        UserResponse loginUser = this.authService.login(loginRequest);

        log.debug("auth login response: {}", loginUser);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(loginUser);
    }
}
