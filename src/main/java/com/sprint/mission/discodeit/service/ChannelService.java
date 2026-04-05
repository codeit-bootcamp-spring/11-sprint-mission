package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;

import java.util.List;
import java.util.UUID;

public interface ChannelService {
    ChannelDto createPublicChannel(PublicChannelCreateRequest dto);
    ChannelDto createPrivateChannel(PrivateChannelCreateRequest dto);
    ChannelDto findById(UUID id);
    List<ChannelDto> findAllByUserId(UUID id);
    void update(UUID id, ChannelUpdateRequest dto);
    void delete(UUID id);
}
