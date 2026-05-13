package com.sprint.mission.discodeit.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

public record PrivateChannelCreateRequest(
    @NotEmpty(message = "초대할 참여자가 최소 1명 이상 필요합니다.")
    List<UUID> participantIds
) {

}
