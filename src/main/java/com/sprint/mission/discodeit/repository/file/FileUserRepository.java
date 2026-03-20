package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
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
public class FileUserRepository implements UserRepository {
    private final Map<UUID, User> data;
    private final String filePath;

    public FileUserRepository(@Value("${discodeit.repository.file-directory:.discodeit}/user.ser")String filePath) {
        this.filePath = filePath;
        this.data = loadFromFile();
    }

    @Override
    public void save(User user) {
        data.put(user.getId(), user);
        saveToFile();
    }

    @Override
    public Optional<User> findById(UUID id) {
        return Optional.ofNullable(loadFromFile().get(id));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(data.values());
    }

    @Override
    public void deleteById(UUID id) {
        data.remove(id);
        saveToFile();
    }

    @SuppressWarnings("unchecked")
    private Map<UUID, User> loadFromFile() {
        File file = new File(filePath);
        if (!file.exists()) {
            return new HashMap<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (Map<UUID, User>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[FileUserRepository] 파일 로드 실패, 새로 시작합니다: " + e.getMessage());
            return new HashMap<>();
        }
    }

    private void saveToFile() {
        File file = new File(filePath);
        file.getParentFile().mkdirs();

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(data);
        } catch (IOException e) {
            throw new RuntimeException("[FileUserRepository] 파일 저장 실패: " + e.getMessage(), e);
        }
    }
}
