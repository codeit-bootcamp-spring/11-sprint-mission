package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class Channel extends BaseEntity {
    private static final long serialVersionUID = 1L;
    private ChannelType type;
    private String name;
    private String description;
    private UUID ownerId;
    private List<UUID> participantIds;


    public Channel(String name, String description, UUID ownerId) {
        super();
        this.type = ChannelType.PUBLIC;
        this.name = name;
        this.description = description;
        this.ownerId = ownerId;
        this.participantIds = new ArrayList<>();
        this.participantIds.add(ownerId);
    }


    public Channel(List<UUID> participantIds) {
        super();
        this.type = ChannelType.PRIVATE;
        this.name = null;
        this.description = null;
        this.ownerId = null;
        this.participantIds = new ArrayList<>(participantIds);
    }

    public boolean addParticipant(UUID userId) {
        if (!participantIds.contains(userId)) {
            participantIds.add(userId);
            return true;
        }
        return false;
    }

    public boolean removeParticipant(UUID userId) {
        return participantIds.remove(userId);
    }

    public void update(String name, String description) {
        this.name = name;
        this.description = description;
        setUpdatedAt(Instant.now());
    }
}