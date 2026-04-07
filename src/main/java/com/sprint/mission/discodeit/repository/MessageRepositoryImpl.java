package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Message;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class MessageRepositoryImpl implements MessageRepository {

  private final Map<UUID, Message> store = new HashMap<>();

  @Override
  public Message save(Message message) {
    store.put(message.getId(), message);
    return message;
  }

  @Override
  public Optional<Message> findById(UUID id) {
    return Optional.ofNullable(store.get(id));
  }

  @Override
  public Optional<Instant> findLatestCreatedAtByChannelId(UUID channelId) {
    return store.values().stream()
        .filter(m -> m.getChannelId().equals(channelId))
        .map(Message::getCreatedAt)
        .max(Comparator.naturalOrder());
  }

  @Override
  public List<Message> findAll() {
    return new ArrayList<>(store.values());
  }

  @Override
  public List<Message> findByAuthorId(UUID authorId) {
    List<Message> result = new ArrayList<>();
    for (Message msg : store.values()) {
      if (authorId.equals(msg.getAuthorId())) {
        result.add(msg);
      }
    }
    return result;
  }

  @Override
  public List<Message> findByChannelId(UUID channelId) {
    List<Message> result = new ArrayList<>();
    for (Message msg : store.values()) {
      if (channelId.equals(msg.getChannelId())) {
        result.add(msg);
      }
    }
    return result;
  }

  @Override
  public void delete(UUID id) {
    store.remove(id);

  }

  @Override
  public void deleteAllByChannelId(UUID channelId) {
    store.entrySet().removeIf(entry -> entry.getValue().getChannelId().equals(channelId));
  }

  @Override
  public void deleteById(UUID id) {
    store.remove(id);
  }
}
