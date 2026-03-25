package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
public class Channel implements Serializable {
    private UUID id;
    private Instant createdAt;
    private Instant updatedAt;
    private String name;
    private String description;
    private static final long serialVersionUID = 1L;
    private ChannelType type;

    public Channel(ChannelType type, String name, String description) {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.name = name;
        this.description = description;
        this.type = type;
    }

    public Instant update(String name) {
        return this.updatedAt = Instant.now();
    }

    @Override
    public String toString() {
        return "Channel{" +
                "id: " + id +
                ", name: " + name +
                ", createdAt: " + createdAt +
                ", updatedAt: " + updatedAt +
                "}";
    }
}
