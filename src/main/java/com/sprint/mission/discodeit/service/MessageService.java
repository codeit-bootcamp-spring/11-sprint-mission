package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.message.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface MessageService {

    MessageDto createMessage(MessageCreateRequest request, List<MultipartFile> attachments);
    MessageDto updateMessage(UUID requestUserId, UUID messageId, MessageUpdateRequest request);
    void deleteMessage(UUID requestUserId, UUID messageId);
    List<MessageDto> findAllByChannelId(UUID requestUserId, UUID channelId);
}
