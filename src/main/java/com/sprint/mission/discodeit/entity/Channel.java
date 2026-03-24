package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class Channel extends Entity{

    private String channelName; //채널 이름
    private UUID ownerId; //채널장
    private ChannelType channelType;
    private String channelDescription;



    public enum ChannelType{
        PUBLIC,
        PRIVATE
    }

    public Channel(String channelName, UUID ownerId, ChannelType channelType, String channelDescription) {
        this.channelName = channelName;
        this.ownerId = ownerId;
        this.channelDescription = channelDescription;
        this.channelType = channelType;
    }

    public void updateChannelName(String channelName) {
        this.channelName = channelName;
        super.updateUpdatedAt();
    }

    public void updateOwner(UUID ownerId){
        this.ownerId = ownerId;
        super.updateUpdatedAt();
    }
    public void updateChannelType(ChannelType channelType){
        this.channelType = channelType;
        super.updateUpdatedAt();
    }
    public void updateChannelDescription(String channelDescription){
        this.channelDescription = channelDescription;
        super.updateUpdatedAt();
    }


}
