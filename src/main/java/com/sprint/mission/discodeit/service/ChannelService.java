package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.ChannelResponse;
import com.sprint.mission.discodeit.dto.ChannelUpdate;
import com.sprint.mission.discodeit.dto.PrivateChannelRequest;
import com.sprint.mission.discodeit.dto.PublicChannelRequest;
import com.sprint.mission.discodeit.entity.Channel;

import java.util.List;
import java.util.UUID;

public interface ChannelService {
    Channel createPublicChannel(PublicChannelRequest request);
    Channel createPrivateChannel(PrivateChannelRequest request);
    ChannelResponse readChannel(UUID id);
    List<ChannelResponse> findAllByUserId(UUID userId);
    void deleteChannel(UUID id);
    void updateChannel(UUID id, ChannelUpdate request);
}
