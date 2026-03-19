package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.util.*;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
public class FileReadStatusRepository implements ReadStatusRepository {
    private final String READSTATUS_FILE = "readStatus.ser";
    private final File file;
    private final Map<UUID, ReadStatus> readStatusMap;

    public FileReadStatusRepository(@Value("${discodeit.repository.file-directory}") String fileDirectory) {
        File dir = new File(fileDirectory);

        if(!dir.exists()) {
            dir.mkdirs();
        }
        this.file = new File(dir, READSTATUS_FILE);
        this.readStatusMap = loadStatus();
    }

    private Map<UUID, ReadStatus> loadStatus() {

        if(!file.exists()) {
            return new HashMap<>();
        }
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (Map<UUID, ReadStatus>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("콘텐츠 로드실패", e);
        }
    }

    private void saveStatus() {
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(readStatusMap);
        } catch (IOException e) {
            throw new RuntimeException("유저 저장실패", e);
        }
    }

    @Override
    public ReadStatus save(ReadStatus status) {
        readStatusMap.put(status.getId(), status);
        saveStatus();
        return status;
    }

    @Override
    public List<ReadStatus> findAllByChannelId(UUID id) {
        return readStatusMap.values().stream()
                .filter(status -> status.getChannelId().equals(id))
                .toList();
    }

    @Override
    public List<ReadStatus> findAllByUserId(UUID id) {
        return readStatusMap.values().stream()
                .filter(status -> status.getUserId().equals(id))
                .toList();
    }

    @Override
    public Optional<ReadStatus> findByUserIdAndChannelId(UUID authorId, UUID channelId) {
        return readStatusMap.values().stream()
                .filter(status -> status.getUserId().equals(authorId) && status.getChannelId().equals(channelId))
                .findFirst();
    }

    @Override
    public Optional<ReadStatus> findById(UUID id) {
        return Optional.ofNullable(readStatusMap.get(id));
    }

    @Override
    public void deleteById(UUID id) {
        if(readStatusMap.containsKey(id)) {
            readStatusMap.remove(id);
            saveStatus();
        }
    }

    @Override
    public void deleteAllByChannelId(UUID channelId) {
        boolean removed = readStatusMap.values().removeIf(status -> status.getChannelId().equals(channelId));

        if(removed) {
            saveStatus();
        }
    }
}
