package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.exception.MessageNotFoundException;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicMessageService implements MessageService {

    private final MessageRepository messageRepo;

    public Message create(String contents, UUID userId, UUID channelId) {
        Message message = new Message(contents, userId, channelId);
        messageRepo.save(message);
        return message;
    }

    public Message findById(UUID id) {
        return messageRepo.findById(id)
                .orElseThrow(() -> new MessageNotFoundException(id));
    }

    public List<Message> findAll() {
        return messageRepo.findAll();
    }

    public void update(Message oldMessage, Message newMessage) {
        oldMessage.setContents(newMessage.getContents());
        oldMessage.setUserId(newMessage.getUserId());
        oldMessage.setChannelId(newMessage.getChannelId());
        oldMessage.update();
        messageRepo.save(oldMessage);
    }

    public void delete(Message message) {
        messageRepo.delete(message);
    }
}
