package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {

  @EntityGraph(attributePaths = {"profile"})
  Optional<User> findById(UUID id);

  @EntityGraph(attributePaths = {"profile"})
  Optional<User> findByUsername(String username);

  @EntityGraph(attributePaths = {"profile"})
  List<User> findAll();

  @Override
  @EntityGraph(attributePaths = {"profile"})
  List<User> findAllById(Iterable<UUID> ids);

  boolean existsByUsername(String username);

  boolean existsByEmail(String email);
}