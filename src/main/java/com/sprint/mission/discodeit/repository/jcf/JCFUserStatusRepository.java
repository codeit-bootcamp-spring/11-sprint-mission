package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "jcf", matchIfMissing = true)
@Repository
public class JCFUserStatusRepository implements UserStatusRepository {

  private final Map<UUID, UserStatus> data;

  public JCFUserStatusRepository() {
    this.data = new HashMap<>();
  }

  @Override
  public void save(UserStatus userStatus) {
    this.data.put(userStatus.getId(), userStatus);
  }

  @Override
  public Optional<UserStatus> findById(UUID id) {
    return Optional.ofNullable(this.data.get(id));
  }

  @Override
  public Optional<UserStatus> findByUserId(UUID userId) {
    return this.data.values().stream()
        .filter(readStatus -> readStatus.getUserId().equals(userId))
        .findFirst();
  }

  @Override
  public List<UserStatus> findAll() {
    return new ArrayList<>(this.data.values());
  }

  @Override
  public boolean existByUserId(UUID userId) {
    return this.data.values().stream()
        .anyMatch(readStatus -> readStatus.getUserId().equals(userId));
  }

  @Override
  public void delete(UserStatus userStatus) {
    this.data.remove(userStatus.getUserId());
  }
}
