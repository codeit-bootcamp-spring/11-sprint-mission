package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.MessageDto;
import com.sprint.mission.discodeit.dto.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface MessageService {

  MessageDto create(MessageCreateRequest request, List<MultipartFile> attachments);

  MessageDto findById(UUID id);

  PageResponse<MessageDto> findAllByChannelId(UUID channelId, Instant cursor, int size);

  MessageDto update(UUID id, MessageUpdateRequest request);

  void delete(UUID id);
}
