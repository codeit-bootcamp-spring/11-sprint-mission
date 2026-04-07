package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.ChannelCreateRequestDto;
import com.sprint.mission.discodeit.dto.ChannelResponseDto;
import com.sprint.mission.discodeit.dto.ChannelUpdateRequestDto;
import java.util.List;
import java.util.UUID;

public interface ChannelService {

  ChannelResponseDto createPublic(ChannelCreateRequestDto dto);

  ChannelResponseDto createPrivate(ChannelCreateRequestDto dto);

  ChannelResponseDto findById(UUID id);

  List<ChannelResponseDto> findAllByUserId(UUID userId);

  ChannelResponseDto update(UUID channelId, ChannelUpdateRequestDto dto);

  void delete(UUID id);

}
