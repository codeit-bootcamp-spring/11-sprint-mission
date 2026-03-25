package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
public class FileMessageRepository implements MessageRepository {

    private static final String FILE_PATH = "message.ser";

    @SuppressWarnings("unchecked")
    private List<Message> load() {
        if (Files.exists(Paths.get(FILE_PATH))) {
            try (
                    FileInputStream fis = new FileInputStream(FILE_PATH);
                    ObjectInputStream ois = new ObjectInputStream(fis);
            ) {
                return (List<Message>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException("파일에서 메시지 데이터 불러오기 실패", e);
            }
        }
        return new ArrayList<>();
    }

    private void saveListToFile(List<Message> data) {
        try (
                FileOutputStream fos = new FileOutputStream(FILE_PATH);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
        ) {
            oos.writeObject(data);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }
    }

    @Override
    public Message save(Message message) {
        List<Message> data = load();
        data.removeIf(u -> u.getId().equals(message.getId()));
        data.add(message);
        saveListToFile(data);
        return message;
    }

    @Override
    public List<Message> findAll() {
        return load();
    }

    @Override
    public Optional<Message> findById(UUID id) {
        List<Message> data = load();
        return data.stream()
                .filter(m -> m.getId().equals(id))
                .findAny();
    }

    @Override
    public Optional<Message> findLatestMessageByChannelId(UUID channelId) {
        List<Message> data = load();
        return data.stream()
                .filter(m -> m.getChannelId().equals(channelId))
                .max(Comparator.comparing(Message::getCreatedAt));
    }

    @Override
    public void delete(UUID id) {
        List<Message> data = load();
        data.removeIf(m -> m.getId().equals(id));
        saveListToFile(data);
    }

    @Override
    public void deleteByChannelId(UUID channelId) {
        List<Message> data = load();
        data.removeIf(m -> m.getChannelId().equals(channelId));
        saveListToFile(data);
    }
}
