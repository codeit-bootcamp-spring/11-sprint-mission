package com.sprint.mission.discodeit.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "LoginRequest")
public record UserLoginRequest(
    @NotBlank(message = "사용자명을 입력해주세요.")
    String username,

    @NotBlank(message = "비밀번호를 입력해주세요.")
    String password) {

}
