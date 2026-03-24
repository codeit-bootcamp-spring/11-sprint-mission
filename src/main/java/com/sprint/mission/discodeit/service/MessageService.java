package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.ChannelMessageList;
import com.sprint.mission.discodeit.dto.CreateMessageRequest;
import com.sprint.mission.discodeit.dto.MessageResponseDto;
import com.sprint.mission.discodeit.dto.MessageUpdate;
import com.sprint.mission.discodeit.entity.Message;

import java.util.List;
import java.util.UUID;

public interface MessageService {
    MessageResponseDto createMessage(CreateMessageRequest request);
    Message readMessage(UUID id);
    public List<Message> readMessagesByChannel(UUID channelId);
    List<MessageResponseDto> findAllByChannelId(ChannelMessageList request);
    void deleteMessage(UUID id);
    void updateMessage(UUID id, MessageUpdate request);
}
