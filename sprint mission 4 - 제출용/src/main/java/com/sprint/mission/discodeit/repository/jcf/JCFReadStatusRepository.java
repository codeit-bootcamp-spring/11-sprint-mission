package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "jcf", matchIfMissing = true)
public class JCFReadStatusRepository implements ReadStatusRepository {
    private final Map<UUID, ReadStatus> data = new ConcurrentHashMap<>();

    @Override
    public ReadStatus create(ReadStatus readStatus){
        data.put(readStatus.getId(), readStatus);
        return readStatus;
    }

    @Override
    public ReadStatus read(UUID id){
        return data.get(id);
    }

    @Override
    public ReadStatus readByUserIdAndChannelId(UUID userId, UUID channelId){
        return data.values().stream()
                .filter(readStatus -> readStatus.getUserId().equals(userId) && readStatus.getChannelId().equals(channelId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<ReadStatus> readAllByChannelId(UUID channelId){
        return data.values().stream()
                .filter(readStatus -> readStatus.getChannelId().equals(channelId))
                .toList();
    }

    @Override
    public ReadStatus update(UUID userId, UUID channelId, Instant lastMessageReadAt){
        ReadStatus readStatus = readByUserIdAndChannelId(userId, channelId);
        if(readStatus==null){
            throw new IllegalArgumentException("존재하지 않는 ReadStatus입니다.");
        }
        readStatus.updateLastMessageReadAt(lastMessageReadAt);
        return readStatus;
    }

    @Override
    public void deleteByChannelId(UUID channelId){
        data.values().removeIf(readStatus -> readStatus.getChannelId().equals(channelId));
    }

    @Override
    public void deleteByUserId(UUID userId){
        data.values().removeIf(readStatus -> readStatus.getUserId().equals(userId));
    }
}
