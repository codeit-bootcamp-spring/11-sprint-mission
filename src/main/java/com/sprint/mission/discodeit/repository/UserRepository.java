package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    @EntityGraph(attributePaths = {"status", "profile"})
    List<User> findAllWithStatusAndProfile();

    @EntityGraph(attributePaths = {"status", "profile"})
    @Query("""
        select u
        from User u
        where u.id = :id
    """)
    Optional<User> findWithStatusAndProfileById(@Param("id") UUID id);
}
