package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.util.*;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
public class FileMessageRepository implements MessageRepository {
    private final String MESSAGE_FILE = "messages.ser";
    private final File file;
    private final Map<UUID, Message> messageMap;

    public FileMessageRepository(@Value("${discodeit.repository.file-directory}") String fileDirectory) {
        File dir = new File(fileDirectory);

        if(!dir.exists()) {
            dir.mkdirs();
        }
        this.file = new File(dir, MESSAGE_FILE);
        this.messageMap = loadMessages();
    }

    private Map<UUID, Message> loadMessages() {

        if(!file.exists()) {
            return new HashMap<>();
        }
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (Map<UUID, Message>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("매세지 로드실패", e);
        }
    }

    private void saveMessage() {
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(messageMap);
        } catch (IOException e) {
            throw new RuntimeException("메세지 저장실패", e);
        }
    }

    @Override
    public Message save(Message message) {
        messageMap.put(message.getId(), message);
        saveMessage();
        return message;
    }

    @Override
    public Optional<Message> findById(UUID id) {
        return Optional.ofNullable(messageMap.get(id));
    }

    @Override
    public List<Message> findAll() {
        return new ArrayList<>(messageMap.values());
    }

    @Override
    public void delete(Message message) {
        if(messageMap.remove(message.getId()) != null) {
            saveMessage();
        }
    }

    @Override
    public Optional<Message> findLatestMessage(UUID channelId) {
        return messageMap.values().stream()
                .filter(message -> message.getChannelId().equals(channelId))
                .max(Comparator.comparing(Message::getCreatedAt));
    }

    @Override
    public void deleteAllByChannelId(UUID channelId) {
        if(messageMap.values().removeIf(m -> m.getChannelId().equals(channelId))) {
            saveMessage();
        }
    }

    @Override
    public List<Message> findByChannelId(UUID channelId) {
        return messageMap.values().stream()
                .filter(message -> message.getChannelId().equals(channelId))
                .toList();
    }
}
