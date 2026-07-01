package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JPAUserRepository extends JpaRepository<User, UUID> {

  @Query("SELECT u FROM User u LEFT JOIN FETCH u.profile")
  @NonNull
  List<User> findAll();

  Optional<User> findByUsername(String username);

  boolean existsByUsername(@NonNull String username);

  boolean existsByEmail(@NonNull String email);

  void deleteById(@NonNull UUID id);

}
