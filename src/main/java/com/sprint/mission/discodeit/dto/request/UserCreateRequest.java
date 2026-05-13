package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 유저 생성 시 필요한 파라미터를 묶어서 전달하는 DTO
public record UserCreateRequest(
    @NotBlank(message = "Username은 필수입니다.")
    @Size(max = 20, message = "Username은 20자 이하여야 합니다.")
    String username,

    @NotBlank(message = "Email은 필수입니다.")
    @Email(message = "Email 형식이 올바르지 않습니다.")
    String email,

    @NotBlank(message = "Password는 필수입니다.")
    String password
) {

}