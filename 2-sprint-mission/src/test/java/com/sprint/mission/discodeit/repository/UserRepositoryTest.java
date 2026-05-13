package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.config.JpaConfig;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaConfig.class)
class UserRepositoryTest {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private TestEntityManager entityManager;

  private User createTestUser(String username, String email) {
    BinaryContent profile = BinaryContent.builder()
        .fileName("profile.jpg")
        .size(1024L)
        .contentType("image/jpeg")
        .build();
    entityManager.persist(profile);

    User user = User.builder()
        .username(username)
        .email(email)
        .password("password123!")
        .profile(profile)
        .build();
    entityManager.persist(user);

    UserStatus status = UserStatus.builder()
        .user(user)
        .lastActiveAt(Instant.now())
        .build();
    entityManager.persist(status);

    return user;
  }

  // findByUsername 테스트
  @Test
  @DisplayName("존재하는 사용자 이름으로 조회 - 사용자 반환")
  void findByUsername_existingUsername_returnsUser() {
    // Given
    String username = "testUser";
    createTestUser(username, "test@example.com");

    entityManager.flush();
    entityManager.clear();

    // When
    Optional<User> foundUser = userRepository.findByUsername(username);

    // Then
    assertThat(foundUser).isPresent();
    assertThat(foundUser.get().getUsername()).isEqualTo(username);
  }

  @Test
  @DisplayName("존재하지 않는 사용자 이름으로 조회 - 빈 Optional 반환")
  void findByUsername_nonExistingUsername_returnsEmpty() {
    // Given
    String nonExistingUsername = "nonExistingUser";

    // When
    Optional<User> foundUser = userRepository.findByUsername(nonExistingUsername);

    // Then
    assertThat(foundUser).isEmpty();
  }

  // existsByEmail 테스트
  @Test
  @DisplayName("존재하는 이메일로 존재 여부 확인 - true 반환")
  void existsByEmail_existingEmail_returnsTrue() {
    // Given
    String email = "test@example.com";
    createTestUser("testUser", email);

    entityManager.flush();
    entityManager.clear();

    // When
    boolean exists = userRepository.existsByEmail(email);

    // Then
    assertThat(exists).isTrue();
  }

  @Test
  @DisplayName("존재하지 않는 이메일로 존재 여부 확인 - false 반환")
  void existsByEmail_nonExistingEmail_returnsFalse() {
    // Given
    String nonExistingEmail = "nonexisting@example.com";

    // When
    boolean exists = userRepository.existsByEmail(nonExistingEmail);

    // Then
    assertThat(exists).isFalse();
  }

  // findAllWithProfileAndStatus 테스트
  @Test
  @DisplayName("프로필 및 상태 정보를 포함한 모든 사용자 조회 - 성공")
  void findAllWithProfileAndStatus_success() {
    // Given
    createTestUser("user1", "user1@example.com");
    createTestUser("user2", "user2@example.com");

    entityManager.flush();
    entityManager.clear();

    // When
    List<User> users = userRepository.findAllWithProfileAndStatus();

    // Then
    assertThat(users).hasSize(2);
    assertThat(users).extracting("username").containsExactlyInAnyOrder("user1", "user2");

    User foundUser1 = users.stream().filter(u -> u.getUsername().equals("user1")).findFirst()
        .orElseThrow();
    User foundUser2 = users.stream().filter(u -> u.getUsername().equals("user2")).findFirst()
        .orElseThrow();

    assertThat(Hibernate.isInitialized(foundUser1.getProfile())).isTrue();
    assertThat(Hibernate.isInitialized(foundUser1.getStatus())).isTrue();
    assertThat(Hibernate.isInitialized(foundUser2.getProfile())).isTrue();
    assertThat(Hibernate.isInitialized(foundUser2.getStatus())).isTrue();
  }
}