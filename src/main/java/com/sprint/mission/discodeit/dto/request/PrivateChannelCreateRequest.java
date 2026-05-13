package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record PrivateChannelCreateRequest(
    @NotNull(message = "참여자 목록을 입력해주세요.")
    @Size(min = 2, message = "Private 채널은 최소 2명 이상이어야 합니다.")
    List<UUID> participantIds
) {

}