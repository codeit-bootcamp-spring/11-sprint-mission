package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.util.*;

@Repository
@ConditionalOnProperty(
        name = "discodeit.repository.type",
        havingValue = "file",
        matchIfMissing = true  // 값이 없으면 기본으로 JCF 사용
)
public class FileBinaryContentRepository implements BinaryContentRepository {
    private final Map<UUID, BinaryContent> data;
    private final String filePath;

    public FileBinaryContentRepository(@Value("${discodeit.repository.file-directory:.discodeit}/binaryContent.ser") String filePath) {
        this.filePath = filePath;
        this.data = loadFromFile();
    }

    @Override
    public void save(BinaryContent binaryContent) {
        data.put(binaryContent.getId(), binaryContent);
        saveToFile();
    }

    @Override
    public BinaryContent findById(UUID id) {
        return data.get(id);
    }

    @Override
    public List<BinaryContent> findAll() {
        return new ArrayList<>(data.values());
    }

    @Override
    public void deleteById(UUID id) {
        data.remove(id);
        saveToFile();
    }

    @SuppressWarnings("unchecked")
    private Map<UUID, BinaryContent> loadFromFile() {
        File file = new File(filePath);
        if (!file.exists()) return new HashMap<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (Map<UUID, BinaryContent>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[FileBinaryContentRepository] 파일 로드 실패: " + e.getMessage());
            return new HashMap<>();
        }
    }

    private void saveToFile() {
        File file = new File(filePath);
        file.getParentFile().mkdirs();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(data);
        } catch (IOException e) {
            throw new RuntimeException("[FileBinaryContentRepository] 파일 저장 실패: " + e.getMessage(), e);
        }
    }
}