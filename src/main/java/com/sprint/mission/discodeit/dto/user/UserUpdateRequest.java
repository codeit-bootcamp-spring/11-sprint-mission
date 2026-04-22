package com.sprint.mission.discodeit.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
    String newUsername,

    @Email(message = "유효한 이메일 형식(예: user@example.com)이 아닙니다.")
    String newEmail,

    @Size(min = 4, message = "비밀번호는 최소 4자 이상이어야 합니다.")
    String newPassword
) {

}