package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.MessageService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JCFMessageService implements MessageService {
    final List<Message> messageList;

    public JCFMessageService() {
        messageList = new ArrayList<>();
    }

    @Override
    public void createMessage(Message message) {
        messageList.add(message);
    }

    @Override
    public Message findMessage(UUID id) {
        for(Message message: messageList) {
            if(message.getId().equals(id)) return message;
        }
        throw new IllegalArgumentException("Message Not Found");
    }

    @Override
    public List<Message> findAllMessage() {
        return messageList;
    }

    @Override
    public void updateMessage(Message oldMessage, Message newMessage) {
        // UUID를 유지하기 위해 remove -> add 하지 않음
        oldMessage.setContents(newMessage.getContents());
    }

    @Override
    public void deleteMessage(Message message) {
        messageList.remove(message);
    }

}
