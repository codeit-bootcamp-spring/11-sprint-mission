package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.ChannelResponse;
import com.sprint.mission.discodeit.dto.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.entity.Channel;

import java.util.List;
import java.util.UUID;

public interface ChannelService {
    ChannelResponse createPublicChannel(PublicChannelCreateRequest request);

    ChannelResponse createPrivateChannel(PrivateChannelCreateRequest request);

    ChannelResponse findById(UUID id);

    List<ChannelResponse> findAllByUserId(UUID userId);

    ChannelResponse update(UUID id, ChannelUpdateRequest request);

    void delete(UUID id);
}
