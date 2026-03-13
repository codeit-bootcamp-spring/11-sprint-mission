package com.sprint.mission.discodeit.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
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

    public void setChannelType(ChannelType channelType) {
        this.channelType = channelType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
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
