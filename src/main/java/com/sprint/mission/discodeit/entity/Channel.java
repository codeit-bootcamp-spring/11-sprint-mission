package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;

@Getter
public class Channel extends BaseEntity implements Serializable {
    private String channelName;
    private String channelDescription;
    private ChannelType channelType;
    private static final long serialVersionUID = 1L;

    public Channel(String channelName, String channelDescription, ChannelType channelType){
        super();
        this.channelName = channelName;
        this.channelDescription = channelDescription;
        this.channelType = channelType;
    }

    public void updateChannel(String channelName, String channelDescription) {
        this.channelName = channelName;
        this.channelDescription = channelDescription;
        updateTimestamp();
    }

    public void validateService() {
        if (this.channelName == null || this.channelName.isBlank()) {
            throw new IllegalArgumentException("채널명이 null이거나 blank입니다.");
        }
    }

    @Override
    public String toString() {
        return
                "Channel ---- " + "[id=" + id + "] " +
                        "[channelName='" + channelName + "'] " +
                        "[channelDescription='" + channelDescription + "']" +
                        " [createdAt='" + createdAt + "']" +
                        " [updatedAt='" + updatedAt + "']";
    }
}
