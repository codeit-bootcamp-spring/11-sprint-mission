package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "jcf", matchIfMissing = true)
public class JCFBinaryContentRepository implements BinaryContentRepository {

  private final Map<UUID, BinaryContent> data = new ConcurrentHashMap<>();

  @Override
  public BinaryContent create(BinaryContent binaryContent) {
    data.put(binaryContent.getId(), binaryContent);
    return binaryContent;
  }

  @Override
  public BinaryContent readById(UUID id) {
    return data.get(id);
  }

  @Override
  public BinaryContent readByUserId(UUID userId) {
    return data.values().stream()
        .filter(binaryContent -> binaryContent.getUserId().equals(userId))
        .findFirst()
        .orElse(null);
  }

  @Override
  public List<BinaryContent> readAllByMessageId(UUID messageId) {
    return data.values().stream()
        .filter(binaryContent -> binaryContent.getMessageId().equals(messageId))
        .toList();
  }

  @Override
  public void delete(UUID id) {
    data.remove(id);
  }

  @Override
  public void deleteByMessageId(UUID messageId) {
    data.values().removeIf(binaryContent -> binaryContent.getMessageId().equals(messageId));
  }

  @Override
  public void deleteByUserId(UUID userId) {
    data.values().removeIf(binaryContent -> binaryContent.getUserId().equals(userId));
  }
}
