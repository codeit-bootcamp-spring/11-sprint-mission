package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
public class FileUserStatusRepository implements UserStatusRepository {

    private static final String FILE_PATH = "userStatus.ser";

    private Map<UUID, UserStatus> load() {
        if (Files.exists(Path.of(FILE_PATH))) {
            try (
                    FileInputStream fis = new FileInputStream(FILE_PATH);
                    ObjectInputStream ois = new ObjectInputStream(fis)
            ) {
                return (Map<UUID, UserStatus>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException("파일에서 UserStatus 불러오기 실패", e);
            }
        }
        return new HashMap<>();
    }

    private void saveMapToFile(Map<UUID, UserStatus> data) {
        try (
                FileOutputStream fos = new FileOutputStream(FILE_PATH);
                ObjectOutputStream oos = new ObjectOutputStream(fos)
        ) {
            oos.writeObject(data);
        } catch (IOException e) {
            throw new RuntimeException("파일에 UserStatus 저장 실패", e);
        }
    }

    @Override
    public UserStatus save(UserStatus status) {
        Map<UUID, UserStatus> data = load();
        data.put(status.getUserId(), status);
        saveMapToFile(data);
        return status;
    }

    @Override
    public Optional<UserStatus> findById(UUID id) {
        return load().values().stream()
                .filter(us -> us.getId().equals(id))
                .findAny();
    }

    @Override
    public Optional<UserStatus> findByUserId(UUID userId) {
        return Optional.ofNullable(load().get(userId));
    }

    @Override
    public List<UserStatus> findAll() {
        return load().values().stream().toList();
    }

    @Override
    public void delete(UUID id) {
        Map<UUID, UserStatus> data = load();
        data.values().removeIf(us -> us.getId().equals(id));
        saveMapToFile(data);
    }

    @Override
    public void deleteByUserId(UUID userId) {
        Map<UUID, UserStatus> data = load();
        data.values().removeIf(us -> us.getUserId().equals(userId));
        saveMapToFile(data);
    }
}
