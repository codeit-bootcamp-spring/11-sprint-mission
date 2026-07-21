package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.security.Role;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, UUID> {

  @EntityGraph(attributePaths = {"profile"})
  Optional<User> findByUsername(String username);

  boolean existsByUsernameOrEmail(String username, String email);

  boolean existsByUsernameAndIdNot(String username, UUID id);

  boolean existsByEmailAndIdNot(String email, UUID userId);

  @Query("SELECT u FROM User u " +
      "LEFT JOIN FETCH u.profile")
  List<User> findAllWithProfile();

  List<User> findAllByRole(Role role);
}
