package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.BinaryContent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class BinaryContentRepositoryImpl implements BinaryContentRepository {

  private final Map<UUID, BinaryContent> store = new HashMap<>();

  @Override
  public BinaryContent save(BinaryContent binaryContent) {
    store.put(binaryContent.getId(), binaryContent);
    return binaryContent;
  }

  @Override
  public Optional<BinaryContent> findById(UUID id) {
    return Optional.ofNullable(store.get(id));
  }

  @Override
  public List<BinaryContent> findAllByIdIn(List<UUID> ids) {
    List<BinaryContent> result = new ArrayList<>();
    for (UUID id : ids) {
      if (store.containsKey(id)) {
        result.add(store.get(id));
      }
    }
    return result;
  }

  @Override
  public void deleteById(UUID id) {
    store.remove(id);
  }

  @Override
  public List<BinaryContent> findAllByMessageId(UUID messageId) {  // ← 추가!
    return store.values().stream()
        .filter(bc -> messageId.equals(bc.getMessageId()))
        .toList();
  }
}
