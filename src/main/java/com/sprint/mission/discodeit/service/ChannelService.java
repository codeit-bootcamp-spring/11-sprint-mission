package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelRequest;

import java.util.List;
import java.util.UUID;

public interface ChannelService {

  ChannelDto createPublicChannel(PublicChannelRequest request);

  ChannelDto createPrivateChannel(PrivateChannelRequest request);

  ChannelDto readChannel(UUID id);

  List<ChannelDto> findAllByUserId(UUID userId);

  void deleteChannel(UUID id);

  ChannelDto updateChannel(UUID id, ChannelUpdateRequest request);
}
