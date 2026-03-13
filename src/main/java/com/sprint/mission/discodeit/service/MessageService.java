package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.MessageCreateRequestDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;

import java.util.List;
import java.util.UUID;

public interface MessageService {
    Message create(MessageCreateRequestDto dto);
    Message findById(UUID id);
    List<Message> findAll();
    void update(UUID id, Message newMessage);
    void delete(Message message);
}
