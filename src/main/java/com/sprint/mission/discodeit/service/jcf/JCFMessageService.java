package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.MessageNotFoundException;
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
    public Message create(String contents, UUID userId, UUID channelId) {
        Message message = new Message(contents, userId, channelId);
        messageList.add(message);
        return message;
    }

    @Override
    public Message findById(UUID id) {
        for(Message message: messageList) {
            if(message.getId().equals(id)) return message;
        }
        throw new MessageNotFoundException(id);
    }

    @Override
    public List<Message> findAll() {
        return messageList;
    }

    @Override
    public void update(UUID id, Message newMessage) {
        // UUID를 유지하기 위해 remove -> add 하지 않음
        Message oldMessage = findById(id);
        oldMessage.setContents(newMessage.getContents());
        oldMessage.setUserId(newMessage.getUserId());
        oldMessage.setChannelId(newMessage.getChannelId());
        oldMessage.update();
    }

    @Override
    public void delete(Message message) {
        messageList.remove(message);
    }

}
