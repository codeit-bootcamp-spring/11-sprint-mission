package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PublicChannelCreateRequest(
        @NotBlank(message = "채널 이름은 필수입니다.")
        @Size(min = 2, max = 30, message = "채널 이름은 2자 이상, 30자 이하여야 합니다.")
        String name,

        @Size(max = 100, message = "설명은 100자를 초과할 수 없습니다.")
        String description
) {}