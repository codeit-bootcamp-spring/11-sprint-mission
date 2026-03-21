package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.channel.ChannelResponseDto;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequestDto;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequestDto;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequestDto;

import java.util.List;
import java.util.UUID;

public interface ChannelService {
    ChannelResponseDto createPublicChannel(PublicChannelCreateRequestDto dto);
    ChannelResponseDto createPrivateChannel(PrivateChannelCreateRequestDto dto);
    ChannelResponseDto findById(UUID id);
    List<ChannelResponseDto> findAllByUserId(UUID id);
    void update(ChannelUpdateRequestDto dto);
    void delete(UUID id);
}
