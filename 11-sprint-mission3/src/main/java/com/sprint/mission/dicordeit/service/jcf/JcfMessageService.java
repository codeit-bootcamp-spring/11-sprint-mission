package com.sprint.mission.dicordeit.service.jcf;

import com.sprint.mission.dicordeit.service.MessageService;
import com.sprint.mission.dicordeit.entity.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JcfMessageService implements MessageService {
    private final List<Message> data = new ArrayList<>();

    @Override
    public Message sendMessage(String content, UUID senderId, UUID channelId) {
        Message createMessage = new Message(content, senderId, channelId);
        data.add(createMessage);

        return createMessage;
    }

    @Override
    public Message correction(UUID messageId, String newContent) {
        return data.stream()
                .filter(user -> user.getId().equals(messageId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void delete(UUID messageId) {
        data.remove(readMessage(messageId));

    }

    @Override
    public Message readMessage(UUID messageId) {
        for (Message message : data) {
            if (message.getId().equals(messageId)) {
                return message;

            }
        }
        return null;
    }

    @Override
    public List<Message> readallMessage() {
        return data;
    }
}
