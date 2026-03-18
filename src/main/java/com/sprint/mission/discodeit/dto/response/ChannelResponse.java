package com.sprint.mission.discodeit.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ChannelResponse {
    private UUID id;
    private String name;
    private String description;
    private Instant lastMessageAt;
    private List<UUID> participantIds;
}
