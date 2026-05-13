package com.sprint.mission.discodeit.dto.userdto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record UserUpdateRequest(
    @NotBlank(message = "유저 이름은 필수 입력값입니다.")
    String newUsername,
    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    @Size(min = 8, max = 30, message = "비밀번호는 8자 이상 30자 미만입니다.")
    String newPassword,
    @NotBlank(message = "유저 이메일은 필수 입력값입니다.")
    @Email(message = "이메일 양식에 맞춰 작성해야 합니다.")
    String newEmail
) {

}
