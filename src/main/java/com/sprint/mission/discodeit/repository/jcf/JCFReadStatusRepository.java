package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;

import java.util.*;

public class JCFReadStatusRepository implements ReadStatusRepository {
    private final Map<UUID, ReadStatus> readStatusMap = new HashMap<>();

    @Override
    public ReadStatus save(ReadStatus status) {
        readStatusMap.put(status.getId(), status);
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
    public void deleteAllByChannelId(UUID id) {
        readStatusMap.values().removeIf(status -> status.getChannelId().equals(id));
    }

    @Override
    public Optional<ReadStatus> findById(UUID id) {
        return Optional.ofNullable(readStatusMap.get(id));
    }

    @Override
    public void deleteById(UUID id) {
        readStatusMap.remove(id);
    }

    @Override
    public Optional<ReadStatus> findByUserIdAndChannelId(UUID authorId, UUID channelId) {
        return readStatusMap.values().stream()
                .filter(status -> status.getUserId().equals(authorId) && status.getChannelId().equals(channelId))
                .findFirst();
    }
}
