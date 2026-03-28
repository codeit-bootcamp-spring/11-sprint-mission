package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.binaryContent.CreateBinaryContentRequest;
import com.sprint.mission.discodeit.dto.request.message.CreateMessageRequest;
import com.sprint.mission.discodeit.dto.request.message.UpdateMessageRequest;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.MessageEditHistory;

import java.util.List;
import java.util.UUID;

public interface MessageService {
    Message createMessage(CreateMessageRequest request, List<CreateBinaryContentRequest> attachments);
    List<Message> findAllByChannelId(UUID channelId);
    void updateMessage(UUID id, UpdateMessageRequest request);
    void deleteMessage(UUID id);

    Message getMessageById(UUID id);
    List<Message> getAllMessages();
    List<MessageEditHistory> getMessageEditHistory(UUID messageId);
    boolean isMessageDeleted(UUID messageId);
}