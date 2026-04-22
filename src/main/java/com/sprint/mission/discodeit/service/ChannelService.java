package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.dto.channel.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import java.util.List;
import java.util.UUID;

public interface ChannelService {

  ChannelDto createPublicChannel(PublicChannelCreateRequest dto);

  ChannelDto createPrivateChannel(PrivateChannelCreateRequest dto);

  List<ChannelDto> findAllByUserId(UUID userId);

  ChannelDto findById(UUID id);

  //List<UUID> getUserIds(UUID id);

  ChannelDto update(UUID id, PublicChannelUpdateRequest dto);

  void delete(UUID id);
}
