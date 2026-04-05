package com.sprint.mission.discodeit.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class ChannelResponseDto {
    private UUID id;
    private String name;
    private String description;
    private boolean isPrivate;
    private UUID channelId;
    private List<UUID> participantUserIds;
    private Instant latesMessageCreatedAt;
}
