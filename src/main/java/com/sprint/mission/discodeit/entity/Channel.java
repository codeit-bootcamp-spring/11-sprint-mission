package com.sprint.mission.discodeit.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import java.time.Instant;

@Getter
public class Channel implements Serializable {
    private static final long serialVersionUID = 1L;

    private final UUID id;
    private final Instant createdAt;
    private Instant updatedAt;

    private String name;
    private String description;

    private final ChannelType type;
    private List<UUID> participantIds;

    public Channel(String name, String description) {
        Instant now = Instant.now();
        this.id = UUID.randomUUID();
        this.createdAt = now;
        this.updatedAt = now;
        this.name = name;
        this.description = description;
        this.type = ChannelType.PUBLIC;
        this.participantIds = new ArrayList<>();
    }

    public Channel(List<UUID> participantIds) {
        Instant now = Instant.now();
        this.id = UUID.randomUUID();
        this.createdAt = now;
        this.updatedAt = now;
        this.name = null;
        this.description = null;
        this.type = ChannelType.PRIVATE;
        this.participantIds = new ArrayList<>(participantIds);
    }

    public void update(String name, String description) {
        if (type == ChannelType.PRIVATE) {
            throw new IllegalArgumentException("PRIVATE 채널은 수정할 수 없습니다.");
        }

        this.name = name;
        this.description = description;
        this.updatedAt = Instant.now();
    }
}
