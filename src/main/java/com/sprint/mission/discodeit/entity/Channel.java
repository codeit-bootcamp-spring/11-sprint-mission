package com.sprint.mission.discodeit.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Channel extends Common{

    private ChannelType channelType;
    private String name;
    private String description;

    public Channel(ChannelType channelType, String name, String description) {
        super();
        this.channelType = channelType;
        this.name = name;
        this.description = description;
    }

    @Override
    public String toString() {
        return "Channel{" +
                "channelType=" + channelType +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
