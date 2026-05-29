package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface MessageService {

  // Create
//    Message create(String content, UUID channelId, UUID userId);
  MessageDto create(MessageCreateRequest dto, List<MultipartFile> attachment);

  // Read
//    Message readAll(UUID id);
  PageResponse<MessageDto> findAllByChannelId(UUID channelId, Instant cursor);

  // Update
//    Message updateContent(UUID id, String newContent);
  MessageDto update(UUID id, MessageUpdateRequest dto);

  // Delete
  void delete(UUID id);
}
