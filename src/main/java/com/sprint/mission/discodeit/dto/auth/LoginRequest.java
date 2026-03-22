package com.sprint.mission.discodeit.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest { //로그인 시
    private String userName;
    private String password;
}