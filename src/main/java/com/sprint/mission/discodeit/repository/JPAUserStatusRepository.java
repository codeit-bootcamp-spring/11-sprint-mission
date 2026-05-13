package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.UserStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JPAUserStatusRepository extends JpaRepository<UserStatus, UUID> {

  @Query("SELECT u FROM UserStatus u JOIN FETCH u.user")
  @NonNull
  List<UserStatus> findAll();

  Optional<UserStatus> findByUserId(UUID userId);

  @Query("SELECT u FROM UserStatus u JOIN FETCH u.user WHERE u.user.id = :userId")
  boolean existsByUserId(UUID userId);


}
