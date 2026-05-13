package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
    @Size(min = 2, max = 50, message = "사용자명은 2자 이상 50자 이하로 입력해주세요.")
    String newUsername,

    @Email(message = "이메일 형식이 올바르지 않습니다.")
    String newEmail,

    @Size(min = 8, message = "비밀번호는 8자 이상 입력해주세요.")
    String newPassword
) {

}