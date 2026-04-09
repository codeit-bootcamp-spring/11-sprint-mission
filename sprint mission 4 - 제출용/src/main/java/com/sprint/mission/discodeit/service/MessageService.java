package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.message.MessageResponse;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface MessageService {

  MessageResponse create(MessageCreateRequest request);

  MessageResponse find(UUID id);

  List<MessageResponse> findAllByChannelId(UUID channelId);

  void update(MessageUpdateRequest request);

  void delete(UUID id);
}