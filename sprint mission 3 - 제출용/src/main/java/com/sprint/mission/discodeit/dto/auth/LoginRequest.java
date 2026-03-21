package com.sprint.mission.discodeit.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginRequest {
    private String userName;
    private String userPassword;
}
