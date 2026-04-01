package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.UserLoginRequest;
import com.sprint.mission.discodeit.service.UserService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public UserDto login(@RequestBody UserLoginRequest request) {
        return userService.login(request);
    }
}
