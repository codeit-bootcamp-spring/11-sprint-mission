package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.User;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryImpl implements UserRepository {

  private final Map<UUID, User> store = new HashMap<>();

  @Override
  public User save(User user) {
    store.put(user.getId(), user);
    return user;
  }

  @Override
  public Optional<User> findById(UUID id) {
    return Optional.ofNullable(store.get(id));
  }

  @Override
  public Optional<User> findByUsername(String username) {
    // username이 null이 아니고, 일치하는  user가 있으면 반환
    return store.values().stream()
        .filter(user -> Objects.equals(user.getUsername(), username))
        .findFirst();
  }

  @Override
  public List<User> findAll() {
    return new ArrayList<>(store.values());
  }
  // username/email 중복 체크용 메서드가 인터페이스에 추가되어 있으면 구현 필요

  @Override
  public boolean existsByUsername(String username) {
    return store.values().stream()
        .anyMatch(user -> Objects.equals(user.getUsername(), username));
  }

  @Override
  public void deleteById(UUID id) {
    store.remove(id);
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return store.values().stream()
        .filter(user -> Objects.equals(user.getEmail(), email))
        .findFirst();
  }

  @Override
  public boolean existsByEmail(String email) {
    return store.values().stream()
        .anyMatch(user -> Objects.equals(user.getEmail(), email));
  }
}

