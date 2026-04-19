package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;

import java.util.UUID;

public interface MessageService {

  MessageDto create(MessageCreateRequest request);

  MessageDto find(UUID id);

  PageResponse<MessageDto> findAllByChannelId(UUID channelId, int page);

  void update(MessageUpdateRequest request);

  void delete(UUID id);
}
