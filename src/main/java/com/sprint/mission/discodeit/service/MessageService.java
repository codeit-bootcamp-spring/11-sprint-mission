package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface MessageService {
    MessageDto createMessage(MessageCreateRequest request, List<MultipartFile> attachments);
    MessageDto updateMessage(UUID messageId, MessageUpdateRequest request);
    void deleteMessage(UUID messageId);
    List<MessageDto> findAllByChannelId(UUID channelId);
}