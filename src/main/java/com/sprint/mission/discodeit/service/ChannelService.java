package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.ChannelResponse;
import com.sprint.mission.discodeit.dto.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.PublicChannelCreateRequest;

import java.util.List;
import java.util.UUID;

public interface ChannelService {
    ChannelResponse createPublicChannel(PublicChannelCreateRequest publicChannelCreateRequest);
    ChannelResponse createPrivateChannel(PrivateChannelCreateRequest privateChannelCreateRequest);
    ChannelResponse findById(UUID id);
    boolean existChannelByName(String name);
    List<ChannelResponse> findAllByUserId(UUID userId);
    ChannelResponse updateChannel(UUID id, ChannelUpdateRequest channelUpdateRequest);
    void deleteChannel(UUID id);
    void joinChannel(UUID id, UUID participantId);
    void leaveChannel(UUID id, UUID participantId);
}
