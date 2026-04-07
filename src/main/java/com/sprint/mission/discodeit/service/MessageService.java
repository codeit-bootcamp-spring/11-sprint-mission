package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.MessageCreateRequestDto;
import com.sprint.mission.discodeit.dto.MessageResponseDto;
import com.sprint.mission.discodeit.dto.MessageUpdateRequestDto;

import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface MessageService {

  MessageResponseDto create(UUID channelId, MessageCreateRequestDto dto,
      List<MultipartFile> attachments);

  List<MessageResponseDto> findAllByChannelId(UUID channelId);

  MessageResponseDto update(UUID channelId, UUID messageId, MessageUpdateRequestDto dto);

  void delete(UUID id, UUID messageId);
}