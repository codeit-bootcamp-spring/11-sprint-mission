package com.sprint.mission.discodeit.dto.authDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
    @NotBlank(message = "유저명은 필수 입력값입니다.")
    String username,
    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    @Size(min = 8, max = 30, message = "비밀번호는 8자리 이상 30자리 이하입니다.")
    String password

) {

}
