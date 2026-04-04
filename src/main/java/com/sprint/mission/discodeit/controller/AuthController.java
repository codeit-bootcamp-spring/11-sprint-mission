package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.constant.EndPoints;
import com.sprint.mission.discodeit.dto.LoginDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController // Json 반환을 위해 @Controller 대신 @ResponseBody를 포함한 @RestController 사용
@RequestMapping(EndPoints.AUTH)
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @RequestMapping(method = RequestMethod.POST) // 보안을 위해 GET이 아닌 POST 사용
    public ResponseEntity<User> login(@RequestBody LoginDto dto) {
        return ResponseEntity.ok(authService.login(dto));
    }
}