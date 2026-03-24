package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageResponse;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface MessageService {
    MessageResponse createMessage(MessageCreateRequest messageCreateRequest, List<BinaryContentCreateRequest> binaryContentCreateRequests);
    List<MessageResponse> findAllByChannelId(UUID channelId);
    MessageResponse updateMessage(UUID id, MessageUpdateRequest messageUpdateRequest, List<BinaryContentCreateRequest> binaryContentCreateRequests);
    void deleteMessage(UUID id);
}
