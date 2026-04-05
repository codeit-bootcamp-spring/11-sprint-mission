package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.ReadStatus;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class ReadStatusRepositoryImpl implements ReadStatusRepository {
    private final Map<UUID, ReadStatus> store = new HashMap<>();

    @Override
    public ReadStatus save(ReadStatus readStatus) {
        store.put(readStatus.getId(), readStatus);
        return readStatus;
    }

    @Override
    public Optional<ReadStatus> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<ReadStatus> findAllByUserId(UUID userId) {
        List<ReadStatus> result = new ArrayList<>();
        for (ReadStatus r : store.values()) {
            if (r.getUserId().equals(userId)) result.add(r);
        }
        return result;
    }

    @Override
    public List<ReadStatus> findAllByChannelId(UUID channelId) {
        List<ReadStatus> result = new ArrayList<>();
        for (ReadStatus r : store.values()) {
            if (r.getChannelId().equals(channelId)) result.add(r);
        }
        return result;
    }

    @Override
    public void deleteAllByChannelId(UUID channelId) {
        store.values().removeIf(r -> r.getChannelId().equals(channelId));
    }

    @Override
    public void deleteById(UUID id) {
        store.remove(id);
    }

    @Override
    public List<ReadStatus> findAllByChannelIdAndMessageId(UUID channelId, UUID messageId) {
        List<ReadStatus> result = new ArrayList<>();
        return result;
    }
}
