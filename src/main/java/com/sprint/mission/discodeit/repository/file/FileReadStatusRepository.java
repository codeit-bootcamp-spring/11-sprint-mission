package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
@ConditionalOnProperty(prefix = "discodeit.repository", name = "type", havingValue = "file")
public class FileReadStatusRepository implements ReadStatusRepository {
    private void saveAll(List<ReadStatus> list) {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream("read_status.ser"))) {
            oos.writeObject(list);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<ReadStatus> loadAll() {
        File file = new File("read_status.ser");
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(file))) {
            return (List<ReadStatus>) ois.readObject();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public void save(ReadStatus readStatus) {
        List<ReadStatus> list = loadAll();
        list.removeIf(rs -> rs.getId().equals(readStatus.getId()));
        list.add(readStatus);
        saveAll(list);
    }

    @Override
    public List<ReadStatus> findByChannelId(UUID channelId) {
        return loadAll().stream().filter(rs ->
                rs.getChannelId().equals(channelId)).toList();
    }

    @Override
    public List<ReadStatus> findByUserId(UUID userId) {
        return loadAll().stream().filter(rs ->
                rs.getUserId().equals(userId)).toList();
    }

    @Override
    public void deleteByChannelId(UUID channelId) {
        List<ReadStatus> list = loadAll();
        list.removeIf(rs ->
                rs.getChannelId().equals(channelId));
        saveAll(list);
    }

    @Override
    public ReadStatus findById(UUID id) {
        return loadAll().stream().filter(rs ->
                rs.getId().equals(id)).findFirst().orElse(null);
    }

    @Override
    public void delete(UUID id) {
        List<ReadStatus> list = loadAll();
        list.removeIf(rs -> rs.getId().equals(id));
        saveAll(list);
    }

    @Override
    public boolean existsByUserIdAndChannelId(UUID userId, UUID channelId) {
        return loadAll().stream().anyMatch(rs ->
                rs.getUserId().equals(userId) &&
                        rs.getChannelId().equals(channelId));
    }
}
