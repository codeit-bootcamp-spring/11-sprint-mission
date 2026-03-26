package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class Channel extends BaseEntity {
    private String channelName;
    private String description;
    private final ChannelType type;
    private final List<UUID> participantIds;

    // PUBLIC 채널 생성
    public Channel(String channelName, String description) {
        super();
        this.channelName = channelName;
        this.description = description;
        this.type = ChannelType.PUBLIC;
        this.participantIds = new ArrayList<>();
    }

    // PRIVATE 채널 생성
    public Channel(List<UUID> participantIds) {
        super();
        this.channelName = null;
        this.description = null;
        this.type = ChannelType.PRIVATE;
        this.participantIds = new ArrayList<>(participantIds);
    }

    public void update(String channelName, String description) {
        this.channelName = channelName;
        this.description = description;
        touch();
    }
}