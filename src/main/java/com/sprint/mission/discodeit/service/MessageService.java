package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.message.CreateMessageRequest;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;

import com.sprint.mission.discodeit.dto.response.PageResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface MessageService {

  MessageDto createMessage(CreateMessageRequest request, List<MultipartFile> attachments);

  PageResponse<MessageDto> readMessagesByChannel(UUID channelId, Instant cursor, int size);

  void deleteMessage(UUID id);

  MessageDto updateMessage(UUID id, MessageUpdateRequest request);
}
