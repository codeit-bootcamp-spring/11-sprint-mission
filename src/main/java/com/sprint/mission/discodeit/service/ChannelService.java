package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.channel.*;
import com.sprint.mission.discodeit.entity.Channel;

import java.util.List;
import java.util.UUID;

public interface ChannelService {
    Channel createPublicChannel(PublicChannelCreateRequest request);
    Channel createPrivateChannel(PrivateChannelCreateRequest request);
    Channel updateChannel(UUID channelId, PublicChannelUpdateRequest request);
    void deleteChannel(UUID channelId);
    List<ChannelDto> findAllByUserId(UUID userId);
}