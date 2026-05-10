package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
class UserStatusRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private UserStatusRepository userStatusRepository;

  private User user;
  private User otherUser;
  private UserStatus userStatus;

  @BeforeEach
  void setUp() {
    user = new User("tester", "tester@example.io", "qwerty", null);
    entityManager.persist(user);

    otherUser = new User("other", "other@example.io", "qwerty", null);
    entityManager.persist(otherUser);

    userStatus = new UserStatus(user);
    entityManager.persistAndFlush(userStatus);
  }

  @Nested
  @DisplayName("find by user id")
  class FindByUserId {

    @Test
    @DisplayName("return user status with existing user id")
    void findByUserId_ExistingUserId_ReturnsUserStatus() {
      // when
      Optional<UserStatus> foundStatus = userStatusRepository.findByUserId(user.getId());

      // then
      assertThat(foundStatus).isPresent();
      assertThat(foundStatus.get().getId()).isEqualTo(userStatus.getId());
    }

    @Test
    @DisplayName("return empty optional with non existing user id")
    void findByUserId_NonExistingUserId_ReturnsEmpty() {
      // when
      Optional<UserStatus> foundStatus = userStatusRepository.findByUserId(otherUser.getId());

      // then
      assertThat(foundStatus).isEmpty();
    }
  }

  @Nested
  @DisplayName("exists by user")
  class ExistsByUser {

    @Test
    @DisplayName("return true with existing user status")
    void existsByUser_ExistingUserStatus_ReturnsTrue() {
      // when
      boolean exists = userStatusRepository.existsByUser(user);

      // then
      assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("return false with non existing user status")
    void existsByUser_NonExistingUserStatus_ReturnsFalse() {
      // when
      boolean exists = userStatusRepository.existsByUser(otherUser);

      // then
      assertThat(exists).isFalse();
    }
  }
}