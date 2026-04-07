package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.baseentity.BaseUpdatableEntity;
import lombok.Getter;

@Getter
public class Channel extends BaseUpdatableEntity {

    private ChannelType channelType;
    private String name;
    private String description;

    public Channel(ChannelType channelType, String name, String description) {
        super();
        this.channelType = channelType;
        this.name = name;
        this.description = description;
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
