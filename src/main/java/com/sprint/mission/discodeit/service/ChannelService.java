package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.ChannelResponseDto;
import com.sprint.mission.discodeit.dto.PrivateChannelCreateRequestDto;
import com.sprint.mission.discodeit.dto.PublicChannelCreateRequestDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;

import java.util.List;
import java.util.UUID;

public interface ChannelService {
    Channel createPublicChannel(PublicChannelCreateRequestDto dto);
    Channel createPrivateChannel(PrivateChannelCreateRequestDto dto);
    ChannelResponseDto findById(UUID id);
    List<ChannelResponseDto> findAllByUserId(UUID id);
    void update(UUID id, Channel newChannel);
    void delete(Channel channel);
}
