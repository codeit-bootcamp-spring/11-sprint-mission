package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// @Repository
public class JCFReadStatusRepository implements ReadStatusRepository {

    private final Map<UUID, ReadStatus> readStatusData = new ConcurrentHashMap<>();

    @Override
    public void save(ReadStatus readStatus) {
        readStatusData.put(readStatus.getId(), readStatus);
    }

    @Override
    public ReadStatus findById(UUID id) {
        ReadStatus readStatus = readStatusData.get(id);
        if (readStatus != null && readStatus.isDeleted()) {
            return null;
        }
        return readStatus;
    }

    @Override
    public List<ReadStatus> findAll() {
        if (readStatusData.isEmpty()) {
            return new ArrayList<>();
        }
        return readStatusData.values().stream()
                .filter(readStatus -> !readStatus.isDeleted())
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        ReadStatus readStatus = findById(id);
        if (readStatus != null) {
            readStatus.softDelete();
            save(readStatus);
        }
    }
}