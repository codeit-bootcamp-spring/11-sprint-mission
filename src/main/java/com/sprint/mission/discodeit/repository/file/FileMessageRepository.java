package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Repository
// application.yaml의 설정값에 따라 Bean을 설정 / name : 설정값의 이름, havingValue : type 지정
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
public class FileMessageRepository implements MessageRepository {
    private final Map<UUID, Message> messages = new HashMap<>();

    private final String fileDirectory;
    private final String fileName = "/messages.ser";
    private final File file;

    public FileMessageRepository(@Value("${discodeit.repository.file-directory}") String fileDirectory) {
        this.fileDirectory = fileDirectory;
        this.file = new File(fileDirectory + fileName);
        load();
    }

    // 저장 메서드 save(직렬화)
    public void save() {
        try (FileOutputStream fos = new FileOutputStream(file);
             ObjectOutputStream oos = new ObjectOutputStream(fos);
        ) {
            oos.writeObject(messages);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 불러오기 메서드 load(역직렬화)
    public void load() {
        if (!file.exists()) return;
        try (FileInputStream fis = new FileInputStream(file);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            Map<UUID, Message> loadChannels = (Map<UUID, Message>) ois.readObject();
            messages.clear(); // 한 번 비우고
            messages.putAll(loadChannels); // 불러온다.(기존에 있던 데이터까지 같이 로드될 수 있기 때문에)
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    public UUID findByContent(String content) {
        for (Map.Entry<UUID, Message> message : messages.entrySet()) {
            if (message.getValue().getContent().equals(content)) {
                return message.getKey();
            }
        }
        return null;
    }

    @Override
    public void insert(Message message) {
        messages.put(message.getId(), message);
        save();
    }

    @Override
    public Message findById(UUID id) {
        Message message = messages.get(id);
        if (message == null) {
            throw new NoSuchElementException("해당 메시지가 존재하지 않습니다. id : " + id);
        }
        return messages.get(id);
    }

    @Override
    public List<Message> findAllByChannelId(UUID channelId) {
        return this.messages.values().stream()
                .filter(message -> message.getChannelId().equals(channelId))
                .collect(Collectors.toList());
    }

    @Override
    public void update(Message message) {
        messages.put(message.getId(), message);
        save();
    }

    @Override
    public void delete(UUID id) {
        messages.remove(id);
        save();
    }

    @Override
    public void deleteAllByChannelId(UUID channelId) {
        messages.values().removeIf(message -> message.getChannelId().equals(channelId));
        save();
    }
}
