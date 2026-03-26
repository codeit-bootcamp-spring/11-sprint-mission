package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class JCFReadStatusRepository implements ReadStatusRepository {

    private final Map<UUID, ReadStatus> data = new HashMap<>();

    @Override
    public ReadStatus save(ReadStatus readStatus) {
        data.put(readStatus.getId(), readStatus);
        return readStatus;
    }

    @Override
    public ReadStatus findById(UUID id) {
        return data.get(id);
    }

    @Override
    public List<ReadStatus> findAll() {
        return new ArrayList<>(data.values());
    }

    @Override
    public void delete(UUID id) {
        data.remove(id);
    }

    @Override
    public ReadStatus findByUserIdAndChannelId(UUID id, UUID channelId) {
        return data.values().stream()
                .filter(readStatus ->
                        readStatus.getUserId().equals(id) &&
                                readStatus.getChannelId().equals(channelId)
                )
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<ReadStatus> findByUserId(UUID id) {
        return data.values().stream()
                .filter(readStatus -> readStatus.getUserId().equals(id))
                .toList();
    }

    @Override
    public List<ReadStatus> findByChannelId(UUID channelId) {
        return data.values().stream()
                .filter(readStatus -> readStatus.getChannelId().equals(channelId))
                .toList();
    }
}