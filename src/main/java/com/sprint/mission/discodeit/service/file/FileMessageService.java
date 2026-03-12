package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.exception.MessageNotFoundException;
import com.sprint.mission.discodeit.service.MessageService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public class FileMessageService extends FileUtil implements MessageService {
    public FileMessageService() {
        super("messages");
    }

    @Override
    public Message create(String contents, UUID userId, UUID channelId) {
        Message message = new Message(contents, userId, channelId);
        save(filePath(message.getId()), message);
        return message;
    }

    @Override
    public Message findById(UUID id) {
        Path path = filePath(id);
        if(!Files.exists(path)) {
            throw new MessageNotFoundException(id);
        }
        return load(path, Message.class);
    }

    @Override
    public List<Message> findAll() {
        return loadAll(directory, Message.class);
    }

    @Override
    public void update(UUID id, Message newMessage) {
        // UUID를 유지하기 위해 remove -> add 하지 않음
        Message oldMessage = findById(id);

        oldMessage.setContents(newMessage.getContents());
        oldMessage.setUserId(newMessage.getUserId());
        oldMessage.setChannelId(newMessage.getChannelId());
        oldMessage.update();

        save(filePath(oldMessage.getId()), oldMessage);
    }

    @Override
    public void delete(Message message) {
        delete(filePath(message.getId()));
    }
}
