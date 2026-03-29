package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.message.*;

import java.util.List;
import java.util.UUID;

public interface MessageService {
    // create
    MessageResponseDTO sendMessage(SendMessageRequestDTO dto);

    MessageResponseDTO updateMessage(UUID is, UpdateMessageRequestDTO dto);

    GetAllMessagesResponseDTO getMessagesByChannel(UUID requestUserId, UUID channelId);

    void deleteMessage(UUID id, DeleteMessageRequestDTO dto);
}
