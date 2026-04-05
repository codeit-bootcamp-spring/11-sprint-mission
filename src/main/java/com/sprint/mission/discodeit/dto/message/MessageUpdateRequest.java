package com.sprint.mission.discodeit.dto.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MessageUpdateRequest(
        @NotBlank(message = "수정할 메세지 내용을 입력해주세요.")
        @Size(max = 2000, message = "메세지는 2000자를 초과할 수 없습니다.")
        String newContent
) {}