package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.base.FileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class FileReadStatusRepository extends FileRepository<ReadStatus> implements ReadStatusRepository {

    private final Map<UUID, List<UUID>> userIdIndex = new HashMap<>();
    private final Map<UUID, List<UUID>> channelIdIndex = new HashMap<>();

    protected FileReadStatusRepository(@Value("${app.data.readstatus-path}")String filePath) {
        super(filePath);
        postLoad();
    }

    @Override
    protected void postLoad() {
        for (ReadStatus rs : dataMap.values()) {
            addToIndex(rs);
        }
    }

    @Override
    protected void postSave(ReadStatus newEntity, ReadStatus oldEntity) {
        if (oldEntity != null) {
            postDelete(oldEntity);
        }

        addToIndex(newEntity);
    }

    @Override
    protected void postDelete(ReadStatus entity) {
        removeFromIndex(userIdIndex, entity.getUserId(), entity.getId());
        removeFromIndex(channelIdIndex, entity.getChannelId(), entity.getId());
    }

    private void addToIndex(ReadStatus rs) {
        userIdIndex.computeIfAbsent(rs.getUserId(), k -> new ArrayList<>()).add(rs.getId());
        channelIdIndex.computeIfAbsent(rs.getChannelId(), k -> new ArrayList<>()).add(rs.getId());
    }

    private void removeFromIndex(Map<UUID, List<UUID>> index, UUID key, UUID value) {
        List<UUID> list = index.get(key);
        if (list != null) {
            list.remove(value);
            if (list.isEmpty())
                index.remove(key);
        }
    }

    @Override
    public List<ReadStatus> findByUserId(UUID userId) {
        readLock.lock();
        try {
            List<UUID> list = userIdIndex.getOrDefault(userId, Collections.emptyList());
            return list.stream()
                    .map(super::findById)
                    .flatMap(Optional::stream)
                    .toList();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public List<ReadStatus> findByChannelId(UUID channelId) {
        readLock.lock();
        try {
            List<UUID> list = channelIdIndex.getOrDefault(channelId, Collections.emptyList());
            return list.stream()
                    .map(super::findById)
                    .flatMap(Optional::stream)
                    .toList();
        } finally {
            readLock.unlock();
        }
    }
}
