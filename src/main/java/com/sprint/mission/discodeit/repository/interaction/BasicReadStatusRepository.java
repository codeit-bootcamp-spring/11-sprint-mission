package com.sprint.mission.discodeit.repository.interaction;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class BasicReadStatusRepository implements ReadStatusRepository {
    private final String READSTATUS_FILE = "readStatus.ser";
    private final Map<UUID, ReadStatus> readStatusMap;

    public BasicReadStatusRepository() {
        this.readStatusMap = loadStatus();
    }

    private Map<UUID, ReadStatus> loadStatus() {
        File file = new File(READSTATUS_FILE);

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
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(READSTATUS_FILE))) {
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
