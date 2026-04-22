package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.channeldto.*;

import com.sprint.mission.discodeit.dto.channeldto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channeldto.request.PublicChanelUpdateRequest;
import com.sprint.mission.discodeit.dto.channeldto.request.PublicChannelCreateRequest;
import java.util.List;
import java.util.UUID;

public interface ChannelService {

  ChannelDto createPrivate(PrivateChannelCreateRequest privateChannelCreateRequest);

  ChannelDto createPublic(PublicChannelCreateRequest publicChannelCreateRequest);


  List<ChannelDto> findAllByUserId(UUID userId);

  ChannelDto updateChannel(UUID channelId,
      PublicChanelUpdateRequest publicChanelUpdateRequest);


  void deleteChannel(UUID channelId);


}
