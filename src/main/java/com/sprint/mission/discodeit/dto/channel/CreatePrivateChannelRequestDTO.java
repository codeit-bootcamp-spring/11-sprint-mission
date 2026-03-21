package com.sprint.mission.discodeit.dto.channel;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public record CreatePrivateChannelRequestDTO(
        @NotNull(message = "요청 사용자 ID는 필수입니다.")
        UUID requestUserId,

        @NotNull
        Set<UUID> userList
) {
}
