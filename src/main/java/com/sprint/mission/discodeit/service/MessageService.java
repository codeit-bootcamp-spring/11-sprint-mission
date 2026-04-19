package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.common.PageResponse;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageResponse;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

public interface MessageService {

  MessageResponse createMessage(MessageCreateRequest messageCreateRequest,
      List<BinaryContentCreateRequest> binaryContentCreateRequests);

  PageResponse<MessageResponse> findAllByChannelId(UUID channelId, Instant cursor,
      Pageable pageable);

  MessageResponse updateMessage(UUID id, MessageUpdateRequest messageUpdateRequest,
      List<BinaryContentCreateRequest> binaryContentCreateRequests);

  void deleteMessage(UUID id);
}
