package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.base.FileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class FileMessageRepository extends FileRepository<Message> implements MessageRepository {

    protected FileMessageRepository(@Value("${app.data.message-path}") String filePath) {
        super(filePath);
        postLoad();
    }

    private final Map<UUID, List<UUID>> userToMessagesIndex = new HashMap<>();
    private final Map<UUID, List<UUID>> channelToMessagesIndex = new HashMap<>();

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
        userToMessagesIndex.computeIfAbsent(m.getUserId(), k -> new ArrayList<>()).add(m.getId());
        channelToMessagesIndex.computeIfAbsent(m.getChannelId(), k -> new ArrayList<>()).add(m.getId());
    }

    private void removeFromIndex(Map<UUID, List<UUID>> index, UUID key, UUID value) {
        List<UUID> list = index.get(key);
        if (list != null) {
            list.remove(value);
            if (list.isEmpty())
                index.remove(key);
        }
    }

    // + 시간순
    @Override
    public List<Message> findAllByUserId(UUID userId) {
        readLock.lock();
        try {
            List<UUID> messageIds = userToMessagesIndex.getOrDefault(userId, Collections.emptyList());
            return messageIds.stream()
                    .map(super::findById)
                    .flatMap(Optional::stream)
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
            List<UUID> messageIds = channelToMessagesIndex.getOrDefault(channelId, Collections.emptyList());
            return messageIds.stream()
                    .map(super::findById)
                    .flatMap(Optional::stream)
                    .sorted(Comparator.comparing(Message::getCreateAt).reversed())
                    .toList();
        } finally {
            readLock.unlock();
        }
    }
}
