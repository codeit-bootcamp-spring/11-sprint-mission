package com.sprint.mission.discodeit.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
    @Size(min = 1, message = "이름 변경을 위해선 1자 이상 입력해야합니다.")
    String newUsername,

    @Email(message = "이메일 형식에 맞춰 입력해야합니다.")
    String newEmail,

    @Size(min = 8, message = "비밀번호 변경을 위해선 8자 이상 입력해야합니다.")
    String newPassword
) {

}
