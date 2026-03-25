package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
@ConditionalOnProperty(prefix = "discodeit.repository", name = "type", havingValue = "jcf", matchIfMissing = true)
public class JCFReadStatusRepository implements ReadStatusRepository {
    private final Map<UUID, ReadStatus> data = new HashMap<>();

    @Override
    public void save(ReadStatus readStatus) {
        data.put(readStatus.getId(), readStatus);
    }

    @Override
    public List<ReadStatus> findByChannelId(UUID channelId) {
        return data.values().stream().filter(rs ->
                rs.getChannelId().equals(channelId)).toList();
    }

    @Override
    public List<ReadStatus> findByUserId(UUID userId) {
        return data.values().stream().filter(rs ->
                rs.getUserId().equals(userId)).toList();
    }

    @Override
    public void deleteByChannelId(UUID channelId) {
        data.values().removeIf(rs ->
                rs.getChannelId().equals(channelId));
    }

    @Override
    public ReadStatus findById(UUID id) {
        return data.get(id);
    }

    @Override
    public void delete(UUID id) {
        data.remove(id);
    }

    @Override
    public boolean existsByUserIdAndChannelId(UUID userId, UUID channelId) {
        return data.values().stream().anyMatch(rs ->
                rs.getUserId().equals(userId) && rs.getChannelId().equals(channelId));
    }
}
