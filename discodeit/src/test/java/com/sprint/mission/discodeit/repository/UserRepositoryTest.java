package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@EnableJpaAuditing
@ActiveProfiles("test")
@DataJpaTest
class UserRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private UserRepository userRepository;

  private UUID userId;
  private String username;
  private String email;
  private User user;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    username = "tester";
    email = "tester@example.io";
    user = new User(username, email, "qwerty", null);
    ReflectionTestUtils.setField(user, "id", userId);
    entityManager.persistAndFlush(user);
  }

  @Nested
  @DisplayName("find by id")
  class FindById {

    @Test
    @DisplayName("return user with existing id")
    void findById_ExistingId_ReturnsUser() {
      // when
      Optional<User> foundUser = userRepository.findById(userId);

      // then
      assertThat(foundUser).isPresent();
      assertThat(foundUser.get().getId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("return empty optional with non existing id")
    void findById_NonExistingId_ReturnsEmptyOptional() {
      // when
      Optional<User> foundUser = userRepository.findById(UUID.randomUUID());

      // then
      assertThat(foundUser).isEmpty();
    }
  }

  @Nested
  @DisplayName("find by username")
  class FindByUsername {

    @Test
    @DisplayName("return user with existing username")
    void findByUsername_ExistingUsername_ReturnsUser() {
      // when
      Optional<User> foundUser = userRepository.findByUsername(username);

      // then
      assertThat(foundUser).isPresent();
      assertThat(foundUser.get().getUsername()).isEqualTo(username);
    }

    @Test
    @DisplayName("return empty optional with non existing username")
    void findByUsername_NonExistingUsername_ReturnsEmpty() {
      // when
      Optional<User> foundUser = userRepository.findByUsername("unknown");

      // then
      assertThat(foundUser).isEmpty();
    }
  }

  @Nested
  @DisplayName("find all")
  class FindAll {

    @Test
    @DisplayName("return all users")
    void findAll_ReturnsAllUsers() {
      // when
      List<User> foundUsers = userRepository.findAll();

      // then
      assertThat(foundUsers).hasSize(1);
      assertThat(foundUsers.get(0).getId()).isEqualTo(userId);
    }
  }

  @Nested
  @DisplayName("find all by id")
  class FindAllById {

    @Test
    @DisplayName("return users with existing ids")
    void findAllById_ExistingIds_ReturnsUsers() {
      // when
      List<User> foundUsers = userRepository.findAllById(List.of(userId));

      // then
      assertThat(foundUsers).hasSize(1);
      assertThat(foundUsers.get(0).getId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("return empty list with non existing ids")
    void findAllById_NonExistingIds_ReturnsEmpty() {
      // when
      List<User> foundUsers = userRepository.findAllById(List.of(UUID.randomUUID()));

      // then
      assertThat(foundUsers).isEmpty();
    }
  }

  @Nested
  @DisplayName("exists by username")
  class ExistsByUsername {

    @Test
    @DisplayName("return true with existing username")
    void existsByUsername_ExistingUsername_ReturnsTrue() {
      // when
      boolean exists = userRepository.existsByUsername(username);

      // then
      assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("return false with non existing username")
    void existsByUsername_NonExistingUsername_ReturnsFalse() {
      // when
      boolean exists = userRepository.existsByUsername("unknown");

      // then
      assertThat(exists).isFalse();
    }
  }

  @Nested
  @DisplayName("exists by email")
  class ExistsByEmail {

    @Test
    @DisplayName("return true with existing email")
    void existsByEmail_ExistingEmail_ReturnsTrue() {
      // when
      boolean exists = userRepository.existsByEmail(email);

      // then
      assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("return false with non existing email")
    void existsByEmail_NonExistingEmail_ReturnsFalse() {
      // when
      boolean exists = userRepository.existsByEmail("other@example.io");

      // then
      assertThat(exists).isFalse();
    }
  }
}