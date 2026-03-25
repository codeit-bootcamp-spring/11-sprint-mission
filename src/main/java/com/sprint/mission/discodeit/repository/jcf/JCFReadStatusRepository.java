package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
// application.yaml의 설정값에 따라 Bean을 설정 / name : 설정값의 이름, havingValue : type 지정, matchIfMissing : 설정이 안되있으면 jcf
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "jcf", matchIfMissing = true)
public class JCFReadStatusRepository implements ReadStatusRepository {
    private final Map<UUID, ReadStatus> readStatuses = new HashMap<>();

    @Override
    public void insert(ReadStatus readStatus) {
        readStatuses.put(readStatus.getId(), readStatus);
    }

    @Override
    public ReadStatus findById(UUID id) {
        ReadStatus readStatus = readStatuses.get(id);
        if (readStatus == null) {
            throw new NoSuchElementException("해당 ReadStatus가 존재하지 않습니다. id : " + id);
        }

        return readStatus;
    }

    @Override
    public List<ReadStatus> findByUserId(UUID userId) {
        return readStatuses.values().stream()
                .filter(readStatus -> readStatus.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<ReadStatus> findByChannelId(UUID channelId) {
        return readStatuses.values().stream()
                .filter(readStatus -> readStatus.getChannelId().equals(channelId))
                .collect(Collectors.toList());
    }

    @Override
    public void update(ReadStatus readStatus) {
        readStatuses.put(readStatus.getId(), readStatus);
    }

    @Override
    public void delete(UUID id) {
        readStatuses.remove(id);
    }

    @Override
    public void deleteAllByChannelId(UUID channelId) {
        readStatuses.values().removeIf(readStatus -> readStatus.getChannelId().equals(channelId));
    }
}
