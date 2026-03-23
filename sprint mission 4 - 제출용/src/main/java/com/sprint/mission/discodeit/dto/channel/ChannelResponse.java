package com.sprint.mission.discodeit.dto.channel;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
// Channel 조회 시 반환 데이터
public class ChannelResponse {
    private UUID channelId;
    private String channelName;
    private String channelDescription;
    private Instant lastMessageAt;
    private List<UUID> userIds;
}
