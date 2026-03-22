package com.sprint.mission.discodeit.dto.channel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant; // Instant로 변경!
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ChannelResponse {
    private UUID id;
    private String channelName;
    private UUID adminId;
    private boolean isPrivate;
    private Instant lastMessageTime;
    private List<UUID> memberIds;
}