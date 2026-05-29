package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.ChannelCreatePrivateRequest;
import com.sprint.mission.discodeit.dto.request.ChannelCreatePublicRequest;
import com.sprint.mission.discodeit.dto.request.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import java.util.List;
import java.util.UUID;

public interface ChannelService {

  // Create
//    Channel create(Channel.ChannelType channelType, String name, String description);
  ChannelDto createPublic(ChannelCreatePublicRequest dto);

  ChannelDto createPrivate(ChannelCreatePrivateRequest dto);

  // Read
//    Channel readAll(UUID id);
  ChannelDto find(UUID id);

  List<ChannelDto> findAllByUserId(UUID userId);

  // Update
//    Channel updateName(UUID id, String newName);
//    Channel updateGroup(UUID id, String newGroup);
//    Channel updateMembersAdd(UUID id, String addMember);
//    Channel updateMembersRemove(UUID id, String removeMember);
  ChannelDto update(UUID id, ChannelUpdateRequest dto);

  // Delete
  void delete(UUID id);
}
