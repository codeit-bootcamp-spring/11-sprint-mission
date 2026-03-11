package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.service.MessageService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class BasicMessageService implements MessageService {

    MessageRepository messageRepo;

    public BasicMessageService(MessageRepository messageRepo) {
        this.messageRepo = messageRepo;
    }

    public Message createMessage(String contents, UUID userId, UUID channelId) {
        Message message = new Message(contents, userId, channelId);
        messageRepo.save(message);
        return message;
    }

    public Message findMessage(UUID id) {
        return messageRepo.load(id);
    }

    public List<Message> findAllMessage() {
        return messageRepo.loadAll();
    }

    public void updateMessage(Message oldMessage, Message newMessage) {
        oldMessage.setContents(newMessage.getContents());
        oldMessage.setUserId(newMessage.getUserId());
        oldMessage.setChannelId(newMessage.getChannelId());
        oldMessage.update();
        messageRepo.save(oldMessage);
    }

    public void deleteMessage(Message message) {
        messageRepo.delete(message);
    }
}
