package com.sprint.mission.discodeit.dto.request.channel;

import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
public class CreatePrivateChannelRequest {
    private List<UUID> userIds;
}
