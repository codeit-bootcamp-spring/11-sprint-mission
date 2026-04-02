package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.*;

@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "jcf", matchIfMissing = true)
@Repository
public class JCFReadStatusRepository implements ReadStatusRepository {
    private final Map<UUID, ReadStatus> data;

    public JCFReadStatusRepository() {
        this.data = new HashMap<>();
    }

    @Override
    public void save(ReadStatus readStatus) {
        this.data.put(readStatus.getId(), readStatus);
    }

    @Override
    public Optional<ReadStatus> findById(UUID id) {
        return Optional.ofNullable(this.data.get(id));
    }

    @Override
    public List<ReadStatus> findAll() {
        return new ArrayList<>(this.data.values());
    }

    @Override
    public List<ReadStatus> findAllByUserId(UUID userId) {
        return this.data.values().stream()
                .filter(readStatus -> readStatus.getUserId().equals(userId))
                .toList();
    }

    @Override
    public List<ReadStatus> findAllByChannelId(UUID channelId) {
        return this.data.values().stream()
                .filter(readStatus -> readStatus.getChannelId().equals(channelId))
                .toList();
    }

    @Override
    public List<ReadStatus> findAllByChannelIdIn(List<UUID> channelIds) {
        Set<UUID> idSet = new HashSet<>(channelIds);
        return this.data.values().stream()
                .filter(readStatus -> idSet.contains(readStatus.getChannelId()))
                .toList();
    }

    @Override
    public boolean existByUserIdAndChannelId(UUID userId, UUID channelId) {
        return this.data.values().stream()
                .anyMatch(readStatus -> readStatus.getUserId().equals(userId) && readStatus.getChannelId().equals(channelId));
    }

    @Override
    public void delete(ReadStatus readStatus) {
        this.data.remove(readStatus.getId());
    }

    @Override
    public void deleteAllByUserId(UUID userId) {
        this.data.values().removeIf(readStatus -> readStatus.getUserId().equals(userId));
    }

    @Override
    public void deleteAllByChannelId(UUID channelId) {
        this.data.values().removeIf(readStatus -> readStatus.getChannelId().equals(channelId));
    }
}
