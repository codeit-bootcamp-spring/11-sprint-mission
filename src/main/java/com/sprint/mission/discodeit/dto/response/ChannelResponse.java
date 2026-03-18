package com.sprint.mission.discodeit.dto.response;

import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
public class ChannelResponse {
    private UUID id;
    private String name;
    private String description;
    private Instant lastMessageAt;
    private List<UUID> participantIds;

    public ChannelResponse(UUID id, String name, String description, Instant lastMessageAt, List<UUID> participantIds) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.lastMessageAt = lastMessageAt;
        this.participantIds = participantIds;
    }
}
