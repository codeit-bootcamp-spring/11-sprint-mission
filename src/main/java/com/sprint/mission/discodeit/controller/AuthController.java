package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.dto.authDto.AuthDto;
import com.sprint.mission.discodeit.dto.userdto.UserInfoDto;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    AuthService authService;

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<UserInfoDto> login (@RequestBody AuthDto authDto){
        return ResponseEntity.status(200).body(authService.login(authDto));
    }


}
