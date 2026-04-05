package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.channeldto.*;

import java.util.List;
import java.util.UUID;

public interface ChannelService {

  CreatedChannelInfo createPrivate(CreatePrivateChannel createPrivateChannel);

  CreatedChannelInfo createPublic(CreatePublicChannel createPublicChannel);

  ChannelInfo findPrivate(UUID channelId, UUID memberId);

  ChannelInfo findPublic(UUID channelId);

  List<ChannelInfo> findAllById(UUID userId);

  CreatedChannelInfo updateChannel(UUID channelId, UpdateChannel updateChannel);


  void deleteChannel(UUID channelId);


}
