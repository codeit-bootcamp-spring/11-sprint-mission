package com.sprint.mission.discodeit.service;

import java.util.UUID;

public interface ChannelService {

    void createChannel(String channelName, String ownerID, String channelId);
    void readChannel(String channelId);
    void readAllChannel();
    public void updateChannelName(String channelId, String channelName);
    public void updateChannelOwner(String channelId, String ownerId);
    public void addMember(String channelId, String memberId);
    public void removeMember(String channelId, String memberId);
    public void deleteChannel(String channelId);
    boolean isExistChannel(String channelId);
    boolean isChannelsMember(String channelId, String memberId);













}
