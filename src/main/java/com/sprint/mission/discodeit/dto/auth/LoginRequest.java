package com.sprint.mission.discodeit.dto.auth;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest { //로그인 시
    @NotBlank
    private String userName;
    @NotBlank
    private String password;
}