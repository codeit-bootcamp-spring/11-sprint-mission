package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "jcf", matchIfMissing = true)
@Repository
public class JCFMessageRepository implements MessageRepository {

  private final Map<UUID, Message> data;

  public JCFMessageRepository() {
    this.data = new HashMap<>();
  }

  @Override
  public void save(Message message) {
    this.data.put(message.getId(), message);
  }

  @Override
  public Optional<Message> findById(UUID id) {
    return Optional.ofNullable(this.data.get(id));
  }

  @Override
  public List<Message> findAll() {
    return new ArrayList<>(this.data.values());
  }

  @Override
  public List<Message> findAllByChannelId(UUID channelId) {
    return this.data.values().stream()
        .filter(message -> message.getChannelId().equals(channelId))
        .toList();
  }

  @Override
  public List<Message> findAllByChannelIdIn(List<UUID> channelIds) {
    Set<UUID> idSet = new HashSet<>(channelIds);
    return this.data.values().stream()
        .filter(message -> idSet.contains(message.getChannelId()))
        .toList();
  }

  @Override
  public void delete(Message message) {
    this.data.remove(message.getId());
  }

  @Override
  public void deleteAllByChannelId(UUID channelId) {
    this.data.values().removeIf(message -> message.getChannelId().equals(channelId));
  }
}
