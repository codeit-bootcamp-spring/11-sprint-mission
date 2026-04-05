package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
public class FileUserRepository implements UserRepository {

    private static final String FILE_PATH = "user.ser";

    @SuppressWarnings("unchecked")
    private Map<UUID, User> load() {
        if (Files.exists(Paths.get(FILE_PATH))) {
            try (
                    FileInputStream fis = new FileInputStream(FILE_PATH);
                    ObjectInputStream ois = new ObjectInputStream(fis)
            ) {
                return (Map<UUID, User>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException("파일에서 유저 데이터 불러오기 실패", e);
            }
        }
        else {
            return new HashMap<>();
        }
    }

    private void saveMapToFile(Map<UUID, User> data) {
        try (
                FileOutputStream fos = new FileOutputStream(FILE_PATH);
                ObjectOutputStream oos = new ObjectOutputStream(fos)
        ) {
            oos.writeObject(data);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }
    }

    @Override
    public User save(User user) {
        Map<UUID, User> data = load();
        data.put(user.getId(), user);
        saveMapToFile(data);
        return user;
    }

    @Override
    public Map<UUID, User> findAll() {
        return load();
    }

    @Override
    public Optional<User> findById(UUID id) {
        Map<UUID, User> data = load();
        return Optional.ofNullable(data.get(id));
    }

    @Override
    public Optional<User> findByName(String name) {
        Map<UUID, User> data = load();
        return data.values().stream()
                .filter(u -> u.getName().equals(name))
                .findAny();
    }

    @Override
    public void delete(UUID id) {
        Map<UUID, User> data = load();
        if (data.remove(id) == null) {
            throw new IllegalArgumentException("[user] 없는 id 입니다.");
        }
        saveMapToFile(data);
    }
}
