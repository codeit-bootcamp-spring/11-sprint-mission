package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.response.PageResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface MessageService {

  MessageDto create(MessageCreateRequest dto,
      List<BinaryContentCreateRequest> binaryContentCreateRequests);

  PageResponse<MessageDto> findAllByChannelId(UUID channelId, Instant cursor);

  MessageDto update(UUID id, MessageUpdateRequest dto);

  void delete(UUID id);
}
