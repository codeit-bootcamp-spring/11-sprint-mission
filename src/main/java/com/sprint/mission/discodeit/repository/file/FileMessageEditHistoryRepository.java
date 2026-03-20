package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.MessageEditHistory;
import com.sprint.mission.discodeit.repository.MessageEditHistoryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.util.*;

@Repository
@ConditionalOnProperty(
        name = "discodeit.repository.type",
        havingValue = "file",
        matchIfMissing = true
)
public class FileMessageEditHistoryRepository implements MessageEditHistoryRepository {
    private final Map<UUID, MessageEditHistory> data;
    private final String filePath;

    public FileMessageEditHistoryRepository(@Value("${discodeit.repository.file-directory:.discodeit}/messageEditHistory.ser")String filePath) {
        this.filePath = filePath;
        this.data = loadFromFile();
    }

    @Override
    public void save(MessageEditHistory history) {
        data.put(history.getId(), history);
        saveToFile();
    }

    @Override
    public Optional<MessageEditHistory> findById(UUID id) {
        return Optional.ofNullable(loadFromFile().get(id));
    }

    @Override
    public List<MessageEditHistory> findAll() {
        return new ArrayList<>(data.values());
    }

    @Override
    public void deleteById(UUID id) {
        data.remove(id);
        saveToFile();
    }

    @SuppressWarnings("unchecked")
    private Map<UUID, MessageEditHistory> loadFromFile() {
        File file = new File(filePath);
        if (!file.exists()) {
            return new HashMap<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (Map<UUID, MessageEditHistory>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[FileMessageEditHistoryRepository] 파일 로드 실패, 새로 시작합니다: " + e.getMessage());
            return new HashMap<>();
        }
    }

    private void saveToFile() {
        File file = new File(filePath);
        file.getParentFile().mkdirs();

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(data);
        } catch (IOException e) {
            throw new RuntimeException("[FileMessageEditHistoryRepository] 파일 저장 실패: " + e.getMessage(), e);
        }
    }
}
