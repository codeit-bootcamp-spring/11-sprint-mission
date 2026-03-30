package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.base.FileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class FileMessageRepository extends FileRepository<Message> implements MessageRepository {

    private final Map<UUID, Set<UUID>> userToMessagesIndex = new HashMap<>();
    private final Map<UUID, Set<UUID>> channelToMessagesIndex = new HashMap<>();

    protected FileMessageRepository(@Value("${app.data.message-path}") String filePath) {
        super(filePath);
        postLoad();
    }

    @Override
    protected void postLoad() {
        for (Message m : dataMap.values()) {
            addToIndex(m);
        }
    }

    @Override
    protected void postSave(Message newEntity, Message oldEntity) {
        if (oldEntity != null) {
            postDelete(oldEntity);
        }

        addToIndex(newEntity);
    }

    @Override
    protected void postDelete(Message entity) {
        removeFromIndex(userToMessagesIndex, entity.getUserId(), entity.getId());
        removeFromIndex(channelToMessagesIndex, entity.getChannelId(), entity.getId());
    }

    private void addToIndex(Message m) {
        userToMessagesIndex.computeIfAbsent(m.getUserId(), k -> new HashSet<>()).add(m.getId());
        channelToMessagesIndex.computeIfAbsent(m.getChannelId(), k -> new HashSet<>()).add(m.getId());
    }

    private void removeFromIndex(Map<UUID, Set<UUID>> index, UUID key, UUID value) {
        Set<UUID> set = index.get(key);
        if (set != null) {
            set.remove(value);
            if (set.isEmpty())
                index.remove(key);
        }
    }

    @Override
    public List<Message> findAllByUserId(UUID userId) {
        readLock.lock();
        try {
            Set<UUID> messageIds = userToMessagesIndex.getOrDefault(userId, Collections.emptySet());
            return messageIds.stream()
                    .map(dataMap::get)
                    .filter(Objects::nonNull)
                    .map(m -> (Message) m.copy()) // 직접 접근이라 복사해주어야함
                    .sorted(Comparator.comparing(Message::getCreateAt).reversed())
                    .toList();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public List<Message> findAllByChannelId(UUID channelId) {
        readLock.lock();
        try {
            Set<UUID> messageIds = channelToMessagesIndex.getOrDefault(channelId, Collections.emptySet());
            return messageIds.stream()
                    .map(dataMap::get)
                    .filter(Objects::nonNull)
                    .map(m -> (Message) m.copy())
                    .sorted(Comparator.comparing(Message::getCreateAt).reversed())
                    .toList();
        } finally {
            readLock.unlock();
        }
    }
}
