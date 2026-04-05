package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.entity.Channel;

import java.util.List;
import java.util.UUID;

public interface ChannelService {
    Channel createPublicChannel(PublicChannelCreateRequest dto);
    Channel createPrivateChannel(PrivateChannelCreateRequest dto);
    List<ChannelResponse> findAllByUserId(UUID userId);
    ChannelResponse findById(UUID id);
    List<UUID> getUserIds(UUID id);
    Channel update(UUID id, ChannelUpdateRequest dto);
    void delete(UUID id);
}
