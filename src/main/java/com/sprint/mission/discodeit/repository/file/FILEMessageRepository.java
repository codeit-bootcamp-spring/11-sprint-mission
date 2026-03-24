package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
public class FILEMessageRepository implements MessageRepository {

    private final FileSaveLoad<Message> saveLoad;
    private final Path directory;

    public FILEMessageRepository(@Value("${discodeit.repository.file-dir}") String path) {

        saveLoad = new FileSaveLoad<>();
        this.directory = Path.of(path+ "/Messages/");
    }

    @Override
    public boolean saveMessage(Message message) {
        if(message == null)
            return false;
        save(idToPath(message.getId()),message);
        return true;
    }

    @Override
    public Optional<Message> getMessage(UUID messageId) {

        Map<UUID,Message> messages = load(directory);
        return Optional.ofNullable(messages.get(messageId));

    }

    @Override
    public Optional<Message> getLastMessagebyChannelId(UUID channelId) {
        Map<UUID,Message> messages = load(directory);
        return messages.values().stream()
                .filter(message -> message.getChannelId().equals(channelId))
                .max(Comparator.comparing(Message::getCreatedAt));

    }

    @Override
    public List<Message> getAllMessage() {
        Map<UUID,Message> messages = load(directory);
        return messages.values().stream().toList();
    }

    @Override
    public List<Message> getAllByChannelId(UUID channelId) {

        Map<UUID,Message> messages = load(directory);
        return messages.values().stream()
                .filter(message -> message.getChannelId().equals(channelId))
                .toList();
    }

    @Override
    public boolean deleteMessage(UUID messageId) {
        if(!isExistMessage(messageId)){
            return false;
        }

        try {
            Files.deleteIfExists(idToPath(messageId));
        }
        catch(IOException e){
            return false;

        }
        return true;


    }

    @Override
    public boolean isExistMessage(UUID messageId) {
        Map<UUID,Message> messages = load(directory);
        return messages.containsKey(messageId);

    }

    @Override
    public boolean channelsMessagedelete(UUID channelId) {
        Map<UUID,Message> messages = load(directory);
        messages.values().stream().filter(message -> message.getChannelId().equals(channelId)).
                forEach(message -> deleteMessage(message.getId()));
        return true;

    }

    private Map<UUID ,Message> load(Path directory) {
       return saveLoad.load(directory);
    }

    private void save(Path filePath, Message message) {
        saveLoad.save(filePath, message);

    }
    private Path idToPath(UUID messageId){

        return directory.resolve(messageId + ".dat");

    }


}
