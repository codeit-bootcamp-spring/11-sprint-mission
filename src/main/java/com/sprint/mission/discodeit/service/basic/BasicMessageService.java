package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Domain.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicMessageService implements MessageService {
    private final MessageRepository messageRepository;
    private final UserService userService;

    @Override
    public UUID create(Message message) {
        if (message == null) {
            throw new IllegalArgumentException("메세지가 null입니다.");
        }
        if (message.getMessageContent() == null || message.getMessageContent().isBlank()) {
            throw new IllegalArgumentException("메세지 내용이 null이거나 blank입니다.");
        }
        if (message.getMessageSender() == null) {
            throw new IllegalArgumentException("sender가 null입니다.");
        }
        if (message.getMessageReceiver() == null) {
            throw new IllegalArgumentException("receiver가 null입니다.");
        }
        if(userService.read(message.getMessageSender().getId()) == null){
            throw new IllegalArgumentException("존재하지 않는 유저입니다.");
        }
        if (userService.read(message.getMessageReceiver().getId()) == null) {
            throw new IllegalArgumentException("존재하지 않는 유저입니다.");
        }
        return messageRepository.create(message);
    }

    @Override
    public Message read(UUID id) {
        return messageRepository.read(id);
    }

    @Override
    public List<Message> readAll() {
        return messageRepository.readAll();
    }

    @Override
    public void update(UUID id, String messageContent) {
        Message message = messageRepository.read(id);
        if (message == null) {
            throw new IllegalArgumentException("존재하지 않는 메세지입니다.");
        }
        if (messageContent == null) {
            throw new IllegalArgumentException("메세지 내용이 null입니다.");
        }
        if (messageContent.isBlank()) {
            throw new IllegalArgumentException("메세지 내용이 blank입니다.");
        }
        message.updateContent(messageContent);
        messageRepository.create(message);
    }

    @Override
    public void delete(UUID id) {
        Message message = messageRepository.read(id);
        if (message == null) {
            throw new IllegalArgumentException("존재하지 않는 메세지입니다.");
        }
        messageRepository.delete(id);
    }
}