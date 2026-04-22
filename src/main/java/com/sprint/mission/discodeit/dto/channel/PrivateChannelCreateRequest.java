package com.sprint.mission.discodeit.dto.channel;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record PrivateChannelCreateRequest(
    @NotNull(message = "참여자 목록은 필수입니다.")
    @Size(min = 2, message = "Private 채널을 생성하려면 최소 2명 이상의 사용자가 필요합니다.")
    List<UUID> participantIds
) {

}