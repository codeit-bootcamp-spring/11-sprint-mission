package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
    @Size(max = 20, message = "Username은 20자 이하여야 합니다.")
    String newUsername,

    @Email(message = "Email 형식이 올바르지 않습니다.")
    String newEmail,

    String newPassword
) {

}
