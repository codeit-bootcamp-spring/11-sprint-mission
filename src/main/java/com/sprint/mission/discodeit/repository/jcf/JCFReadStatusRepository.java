package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.*;


@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "jcf")
public class JCFReadStatusRepository implements ReadStatusRepository {

  private final Map<Pair, ReadStatus> data;

  public JCFReadStatusRepository() {
    data = new HashMap<>();
  }


  @Override
  public boolean save(ReadStatus readStatus) {
    Pair pair = new Pair(readStatus.getUserId(), readStatus.getChannelId());
    data.put(pair, readStatus);
    return true;
  }

  @Override
  public Optional<ReadStatus> get(UUID userId, UUID channelId) {
    Pair pair = new Pair(userId, channelId);
    return Optional.ofNullable(data.get(pair));
  }

  @Override
  public Optional<ReadStatus> get(UUID readStatusId) {
    return data.values().stream()
        .filter(readStatus -> readStatus.getId().equals(readStatusId))
        .findFirst();


  }


  @Override
  public List<ReadStatus> getAll() {
    return data.values().stream().toList();
  }

  @Override
  public List<ReadStatus> getAllByUserId(UUID userId) {
    return data.values().stream()
        .filter(readStatus -> readStatus.getUserId().equals(userId))
        .toList();
  }

  public List<ReadStatus> getAllByChannelId(UUID channelId) {

    return data.values().stream()
        .filter(readStatus -> readStatus.getChannelId().equals(channelId))
        .toList();


  }

  @Override
  public boolean delete(UUID userId, UUID channelId) {
    Pair pair = new Pair(userId, channelId);
    return data.remove(pair) != null;
  }

  @Override
  public boolean isExist(UUID userId, UUID channelId) {
    return data.containsKey(new Pair(userId, channelId));
  }


  record Pair(UUID userId, UUID channelId) {

  }


}
