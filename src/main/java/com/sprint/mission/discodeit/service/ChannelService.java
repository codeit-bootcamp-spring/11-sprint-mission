package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import java.util.List;
import java.util.UUID;

public interface ChannelService {

  ChannelDto createPublic(PublicChannelCreateRequest request);

  ChannelDto createPrivate(PrivateChannelCreateRequest request);

  ChannelDto findById(UUID id);

  List<ChannelDto> findAllByUserId(UUID userId);

  ChannelDto update(UUID channelId, PublicChannelUpdateRequest request);

  void delete(UUID id);
}
