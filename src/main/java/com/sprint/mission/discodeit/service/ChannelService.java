package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.ChannelResponseDto;
import com.sprint.mission.discodeit.dto.ChannelUpdateRequestDto;
import com.sprint.mission.discodeit.dto.PrivateChannelCreateRequestDto;
import com.sprint.mission.discodeit.dto.PublicChannelCreateRequestDto;

import java.util.List;
import java.util.UUID;

public interface ChannelService {
    ChannelResponseDto createPublic(PublicChannelCreateRequestDto dto);

    ChannelResponseDto createPrivate(PrivateChannelCreateRequestDto dto);

    ChannelResponseDto findById(UUID id);

    List<ChannelResponseDto> findAllByUserId(UUID userId);

    ChannelResponseDto update(UUID channelId, ChannelUpdateRequestDto dto);

    void delete(UUID id);

}
