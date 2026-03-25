package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.ChannelCreatePrivateDto;
import com.sprint.mission.discodeit.dto.ChannelCreatePublicDto;
import com.sprint.mission.discodeit.dto.ChannelReadDto;
import com.sprint.mission.discodeit.dto.ChannelUpdateDto;
import com.sprint.mission.discodeit.entity.Channel;

import java.util.List;
import java.util.UUID;

public interface ChannelService {
    // Create
//    Channel create(Channel.ChannelType channelType, String name, String description);
    Channel createPublic(ChannelCreatePublicDto dto);
    Channel createPrivate(ChannelCreatePrivateDto dto);

    // Read
//    Channel readAll(UUID id);
    ChannelReadDto find(UUID id);
    List<ChannelReadDto> findAllByUserId(UUID userId);

    // Update
//    Channel updateName(UUID id, String newName);
//    Channel updateGroup(UUID id, String newGroup);
//    Channel updateMembersAdd(UUID id, String addMember);
//    Channel updateMembersRemove(UUID id, String removeMember);
    Channel update(UUID id, ChannelUpdateDto dto);

    // Delete
    void delete(UUID id);
}
