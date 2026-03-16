package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.ChannelResponseDto;
import com.sprint.mission.discodeit.dto.ChannelUpdateRequestDto;
import com.sprint.mission.discodeit.dto.PrivateChannelCreateRequestDto;
import com.sprint.mission.discodeit.dto.PublicChannelCreateRequestDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;

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
