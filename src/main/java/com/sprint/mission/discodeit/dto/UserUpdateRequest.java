package com.sprint.mission.discodeit.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
    @Size(min = 2, max = 20, message = "사용자명은 2~20자여야 합니다.")
    String newUsername,

    @Email(message = "이메일 형식이 올바르지 않습니다.")
    String newEmail,

    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
    String newPassword) {

}
