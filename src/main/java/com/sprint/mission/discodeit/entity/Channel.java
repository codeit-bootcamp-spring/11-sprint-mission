package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class Channel extends BaseEntity {
    private static final long serialVersionUID = 1L;
    private ChannelType type;
    private String name;
    private String description;
    private UUID ownerId;


    public Channel(String name, String description, UUID ownerId) {
        super();
        this.type = ChannelType.PUBLIC;
        this.name = name;
        this.description = description;
        this.ownerId = ownerId;
    }


    public Channel() {
        super();
        this.type = ChannelType.PRIVATE;
        this.name = null;
        this.description = null;
        this.ownerId = null;
    }

    public void update(String name, String description) {
        this.name = name;
        this.description = description;
        setUpdatedAt(Instant.now());
    }
}