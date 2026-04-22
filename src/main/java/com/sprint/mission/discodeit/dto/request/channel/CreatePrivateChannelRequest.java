package com.sprint.mission.discodeit.dto.request.channel;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class CreatePrivateChannelRequest {
    private List<UUID> userIds;
}
