package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.util.FileIOUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
@Repository
public class FileMessageRepository implements MessageRepository {
    private final FileIOUtil<Message> fileIOUtil;

    public FileMessageRepository() {
        this.fileIOUtil = new FileIOUtil<>(Message.class);
    }

    @Override
    public void save(Message message) {
        this.fileIOUtil.save(message);
    }

    @Override
    public Optional<Message> findById(UUID id) {
        return Optional.ofNullable(this.fileIOUtil.findById(id));
    }

    @Override
    public List<Message> findAll() {
        return this.fileIOUtil.findAll();
    }

    @Override
    public List<Message> findAllByChannelId(UUID channelId) {
        return this.fileIOUtil.findAll().stream()
                .filter(message -> message.getChannel().getId().equals(channelId))
                .toList();
    }

    @Override
    public void delete(Message message) {
        this.fileIOUtil.delete(message);
    }

    @Override
    public void deleteAllByChannelId(UUID channelId) {
        this.findAllByChannelId(channelId)
                .forEach(this.fileIOUtil::delete);
    }
}
