package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.service.AuthService;
import com.sprint.mission.discodeit.service.dto.user.UserLoginRequest;
import com.sprint.mission.discodeit.service.dto.user.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public UserDto login(@RequestBody UserLoginRequest request) {
        return authService.login(request);
    }
}
