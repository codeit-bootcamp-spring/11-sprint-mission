package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.channel.*;
import com.sprint.mission.discodeit.entity.Channel;

import java.util.List;
import java.util.UUID;

public interface ChannelService {

    Channel createPublicChannel(UUID requestUserId, PublicChannelCreateRequest request);
    Channel createPrivateChannel(UUID requestUserId, PrivateChannelCreateRequest request);
    Channel updateChannel(UUID requestUserId, UUID channelId, PublicChannelUpdateRequest request);
    void deleteChannel(UUID requestUserId, UUID channelId);
    List<ChannelDto> findAllByUserId(UUID userId);
}
