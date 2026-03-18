package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.channel.CreatePrivateChannelRequest;
import com.sprint.mission.discodeit.dto.request.channel.CreatePublicChannelRequest;
import com.sprint.mission.discodeit.dto.request.channel.UpdateChannelRequest;
import com.sprint.mission.discodeit.dto.response.ChannelResponse;

import java.util.List;
import java.util.UUID;

public interface ChannelService {
    ChannelResponse createPublicChannel(CreatePublicChannelRequest request);
    ChannelResponse createPrivateChannel(CreatePrivateChannelRequest request);
    ChannelResponse getChannelById(UUID id);
    List<ChannelResponse> findAllByUserId(UUID userId);
    ChannelResponse updateChannel(UUID id, UpdateChannelRequest request);
    void deleteChannel(UUID id);

    boolean joinChannel(UUID channelId, UUID userId);
    boolean leaveChannel(UUID channelId, UUID userId);
    boolean kickUser(UUID channelId, UUID ownerId, UUID targetUserId);
    List<UUID> getChannelParticipants(UUID channelId);
}