package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@RequestMapping("/api/auth")
@ResponseBody
@RequiredArgsConstructor
@Controller
public class AuthController {
    private final AuthService authService;
}
