package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.channel.ChannelInfoDto;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateDto;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateDto;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateDto;
import com.sprint.mission.discodeit.entity.Channel;

import java.util.List;
import java.util.UUID;

public interface ChannelService {
    Channel createPublicChannel(PublicChannelCreateDto dto);
    Channel createPrivateChannel(PrivateChannelCreateDto dto);
    List<ChannelInfoDto> findAllByUserId(UUID userId);
    ChannelInfoDto findById(UUID id);
    ChannelInfoDto update(ChannelUpdateDto dto);
    void delete(UUID id);
}
