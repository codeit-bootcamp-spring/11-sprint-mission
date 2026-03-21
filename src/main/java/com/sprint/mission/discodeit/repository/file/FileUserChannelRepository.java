package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.UserChannel;
import com.sprint.mission.discodeit.repository.UserChannelRepository;
import com.sprint.mission.discodeit.repository.base.FileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class FileUserChannelRepository extends FileRepository<UserChannel> implements UserChannelRepository {

    protected FileUserChannelRepository(@Value("${app.data.userchannel-path}") String filePath) {
        super(filePath);
        postLoad();
    }

    private final Map<UUID, List<UUID>> userToChannelsIndex = new HashMap<>();
    private final Map<UUID, List<UUID>> channelToUsersIndex = new HashMap<>();
    private final Map<String, UUID> exactMatchIndex = new HashMap<>();

    @Override
    protected void postLoad() {
        for (UserChannel uc : dataMap.values()) {
            addToIndex(uc);
        }
    }

    @Override
    protected void postSave(UserChannel newEntity, UserChannel oldEntity) {
        if (oldEntity != null) {
            postDelete(oldEntity);
        }

        addToIndex(newEntity);
    }

    @Override
    protected void postDelete(UserChannel entity) {
        removeFromIndex(userToChannelsIndex, entity.getUserId(), entity.getId());
        removeFromIndex(channelToUsersIndex, entity.getChannelId(), entity.getId());
        exactMatchIndex.remove(makeKey(entity.getUserId(), entity.getChannelId()));
    }

    private String makeKey(UUID userId, UUID channelId) {
        return userId.toString() + ":" + channelId.toString();
    }

    private void addToIndex(UserChannel uc) {
        userToChannelsIndex.computeIfAbsent(uc.getUserId(), k -> new ArrayList<>()).add(uc.getId());
        channelToUsersIndex.computeIfAbsent(uc.getChannelId(), k -> new ArrayList<>()).add(uc.getId());
        exactMatchIndex.put(makeKey(uc.getUserId(), uc.getChannelId()), uc.getId());
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
    public Optional<UserChannel> findByUserIdAndChannelId(UUID userId, UUID channelId) {
        readLock.lock();
        try {
            UUID targetId = exactMatchIndex.get(makeKey(userId, channelId));
            return targetId != null ? super.findById(targetId) : Optional.empty();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public List<UserChannel> findAllByUserId(UUID userId) {
        readLock.lock();
        try {
            List<UUID> list = userToChannelsIndex.getOrDefault(userId, Collections.emptyList());
            return list.stream()
                    .map(super::findById)
                    .flatMap(Optional::stream)
                    .toList();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public List<UserChannel> findAllByChannelId(UUID channelId) {
        readLock.lock();
        try {
            List<UUID> list = channelToUsersIndex.getOrDefault(channelId, Collections.emptyList());
            return list.stream()
                    .map(super::findById)
                    .flatMap(Optional::stream)
                    .toList();
        } finally {
            readLock.unlock();
        }
    }
}
