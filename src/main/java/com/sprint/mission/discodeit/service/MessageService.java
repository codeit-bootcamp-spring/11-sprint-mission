package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.messagedto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.messagedto.MessageDto;
import com.sprint.mission.discodeit.dto.messagedto.request.MessageUpdateRequest;

import com.sprint.mission.discodeit.dto.response.PageResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface MessageService {

  MessageDto create(MessageCreateRequest messageCreateRequest, List<MultipartFile> files);

  MessageDto find(UUID messageId);

  PageResponse<MessageDto> findAllByChannelId(UUID channelId, Pageable pageable, Instant cursor);

  MessageDto update(UUID messageId, MessageUpdateRequest messageUpdateRequest);

  void delete(UUID messageId);


}
