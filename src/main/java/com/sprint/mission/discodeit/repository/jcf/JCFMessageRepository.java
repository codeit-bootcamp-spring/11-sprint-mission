package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class JCFMessageRepository implements MessageRepository {

    private final Map<String, Message> data ;

    public JCFMessageRepository() {
        data = new HashMap<>();
    }


    @Override
    public boolean saveMessage(Message message) {

        if(data.containsKey(message.getMessageId()))
            return false;

        data.put(message.getMessageId(),message);
        return true;

    }

    @Override
    public Message getMessage(String messageId) {
        return data.getOrDefault(messageId,null);
    }

    @Override
    public List<Message> getAllMessage() {
        return data.values().stream().toList();
    }

    @Override
    public boolean updateMessage(Message message) {

        data.put(message.getMessageId(), message);
        return true;

    }

    @Override

    public boolean deleteMessage(String messageId) {

        data.remove(messageId);
        return true;
    }

    @Override
    public boolean isExistMessage(String messageId) {
        return data.containsKey(messageId);
    }


    //메시지 전체 삭제 (채널 삭제때)
    @Override
    public boolean channelsMessagedelete(String channelId) {

        data.values().stream()
                .filter(msg -> msg.getChannelId().equals(channelId))
                .forEach(msg-> deleteMessage(msg.getMessageId()));


        return true;
    }
}
