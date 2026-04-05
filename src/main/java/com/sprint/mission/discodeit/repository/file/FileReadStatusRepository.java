package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
public class FileReadStatusRepository implements ReadStatusRepository {

    private static final String FILE_PATH = "readStatus.ser";

    private Map<UUID, ReadStatus> load() {
        if (Files.exists(Path.of(FILE_PATH))) {
            try (
                    FileInputStream fis = new FileInputStream(FILE_PATH);
                    ObjectInputStream ois = new ObjectInputStream(fis)
            ) {
                return (Map<UUID, ReadStatus>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException("파일에서 ReadStatus 불러오기 실패", e);
            }
        }
        return new HashMap<>();
    }

    private void saveMapToFile(Map<UUID, ReadStatus> data) {
        try (
                FileOutputStream fos = new FileOutputStream(FILE_PATH);
                ObjectOutputStream oos = new ObjectOutputStream(fos)
                ) {
            oos.writeObject(data);
        } catch (IOException e) {
            throw new RuntimeException("파일에 ReadStatus 저장 실패", e);
        }
    }

    @Override
    public ReadStatus save(ReadStatus status) {
        Map<UUID, ReadStatus> data = load();
        data.put(status.getId(), status);
        saveMapToFile(data);
        return status;
    }

    @Override
    public List<ReadStatus> findAllByChannelId(UUID channelId) {
        return load().values().stream()
                .filter(rs -> rs.getChannelId().equals(channelId))
                .toList();
    }

    @Override
    public List<ReadStatus> findAllByUserId(UUID userId) {
        return load().values().stream()
                .filter(rs -> rs.getUserId().equals(userId))
                .toList();
    }

    @Override
    public Optional<ReadStatus> find(UUID id) {
        return Optional.ofNullable(load().get(id));
    }

    @Override
    public boolean existsByUserIdAndChannelId(UUID userId, UUID channelId) {
        return load().values().stream()
                .anyMatch(rs -> rs.getUserId().equals(userId) && rs.getChannelId().equals(channelId));
    }

    @Override
    public void delete(UUID id) {
        Map<UUID, ReadStatus> data = load();
        data.remove(id);
        saveMapToFile(data);
    }

    @Override
    public void deleteByChannelId(UUID channelId) {
        Map<UUID, ReadStatus> data = load();
        data.values().removeIf(rs -> rs.getChannelId().equals(channelId));
        saveMapToFile(data);
    }
}
