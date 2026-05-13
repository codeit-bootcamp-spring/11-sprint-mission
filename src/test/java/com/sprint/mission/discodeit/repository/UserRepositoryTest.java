package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.config.TestJpaConfig;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestJpaConfig.class)
class UserRepositoryTest {

  @Autowired
  private UserRepository userRepository;
  @Autowired
  private UserStatusRepository userStatusRepository;

  private User savedUser;

  @BeforeEach
  void setUp() {
    User user = new User("testuser", "test@example.com", "password123", null);
    savedUser = userRepository.save(user);
    UserStatus status = new UserStatus(savedUser, Instant.now());
    userStatusRepository.save(status);
  }

  @Test
  void findByUsername_succeeds() {
    Optional<User> result = userRepository.findByUsername("testuser");

    assertThat(result).isPresent();
    assertThat(result.get().getUsername()).isEqualTo("testuser");
    assertThat(result.get().getEmail()).isEqualTo("test@example.com");
  }

  @Test
  void findByUsername_whenNotExists_returnsEmpty() {
    Optional<User> result = userRepository.findByUsername("nonexistent");

    assertThat(result).isEmpty();
  }

  @Test
  void existsByUsername_whenExists_returnsTrue() {
    boolean result = userRepository.existsByUsername("testuser");

    assertThat(result).isTrue();
  }

  @Test
  void existsByUsername_whenNotExists_returnsFalse() {
    boolean result = userRepository.existsByUsername("nonexistent");

    assertThat(result).isFalse();
  }

  @Test
  void existsByEmail_whenExists_returnsTrue() {
    boolean result = userRepository.existsByEmail("test@example.com");

    assertThat(result).isTrue();
  }

  @Test
  void existsByEmail_whenNotExists_returnsFalse() {
    boolean result = userRepository.existsByEmail("other@example.com");

    assertThat(result).isFalse();
  }

  @Test
  void findAllWithProfileAndStatus_succeeds() {
    List<User> users = userRepository.findAllWithProfileAndStatus();

    assertThat(users).hasSize(1);
    assertThat(users.get(0).getUsername()).isEqualTo("testuser");
  }

  @Test
  void findAllWithProfileAndStatus_excludesUsersWithoutStatus() {
    User userWithoutStatus = new User("nostatususer", "nostatus@example.com", "pass", null);
    userRepository.save(userWithoutStatus);

    List<User> users = userRepository.findAllWithProfileAndStatus();

    assertThat(users).hasSize(1);
    assertThat(users.get(0).getUsername()).isEqualTo("testuser");
  }
}