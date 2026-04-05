package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.util.FileIOUtil;
import com.sprint.mission.discodeit.util.FileLockProvider;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
@Repository
public class FileMessageRepository implements MessageRepository {

  private final FileIOUtil<Message> fileIOUtil;

  public FileMessageRepository(FileLockProvider fileLockProvider) {
    this.fileIOUtil = new FileIOUtil<>(Message.class, fileLockProvider);
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
        .filter(message -> message.getChannelId().equals(channelId))
        .toList();
  }

  @Override
  public List<Message> findAllByChannelIdIn(List<UUID> channelIds) {
    Set<UUID> idSet = new HashSet<>(channelIds);
    return this.fileIOUtil.findAll().stream()
        .filter(message -> idSet.contains(message.getChannelId()))
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
