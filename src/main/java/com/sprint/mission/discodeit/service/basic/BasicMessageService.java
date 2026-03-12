package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.MessageEditHistory;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.service.MessageService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class BasicMessageService implements MessageService {
    private final MessageRepository messageRepository;

    public BasicMessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Override
    public Message sendDirectMessage(User sender, User receiver, String content) {
        Message message = new Message(sender, receiver, content);
        messageRepository.save(message);
        sender.getSentMessages().add(message);
        receiver.getReceivedMessages().add(message);
        return message;
    }

    @Override
    public Message sendChannelMessage(User sender, Channel channel, String content) {
        Message message = new Message(sender, channel, content);
        messageRepository.save(message);
        sender.getSentMessages().add(message);
        channel.addMessage(message);
        return message;
    }

    @Override
    public Message getMessageById(UUID id) {
        return messageRepository.findById(id);
    }

    @Override
    public List<Message> getAllMessages() {
        return messageRepository.findAll();
    }

    @Override
    public List<Message> getMessagesBySender(User sender) {
        return new ArrayList<>(sender.getSentMessages());
    }

    @Override
    public List<Message> getMessagesInChannel(Channel channel) {
        return new ArrayList<>(channel.getMessages());
    }

    @Override
    public void updateMessage(UUID messageId, String newContent) {
        Message message = messageRepository.findById(messageId);
        if (message != null && !message.isDeleted()) {
            message.update(newContent);
            messageRepository.save(message);
        }
    }

    @Override
    public void deleteMessage(UUID messageId) {
        Message message = messageRepository.findById(messageId);
        if (message != null) {
            message.delete();
            messageRepository.save(message);
        }
    }

    @Override
    public List<MessageEditHistory> getMessageEditHistory(UUID messageId) {
        Message message = messageRepository.findById(messageId);
        if (message == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(message.getEditHistories());
    }

    @Override
    public boolean isMessageDeleted(UUID messageId) {
        Message message = messageRepository.findById(messageId);
        return message != null && message.isDeleted();
    }
}
