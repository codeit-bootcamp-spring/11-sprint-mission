package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@EnableJpaAuditing
@AutoConfigureTestDatabase(replace = Replace.NONE)
class UserRepositoryTest {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private TestEntityManager em;

  private User savedUser;

  @BeforeEach
  void setUp() {
    BinaryContent profile = new BinaryContent("test.png", "image/png", 1024L);
    em.persist(profile);

    User user = new User("test", "test@test.com", "password123", profile);
    savedUser = em.persistAndFlush(user);

    UserStatus status = new UserStatus(savedUser);
    em.persistAndFlush(status);

    em.clear();
  }

  // --- findAll() ---
  @Test
  @DisplayName("전체 사용자 조회 시 프로필과 상태를 EntityGraph로 함께 로드")
  void findAll_withEntityGraph_loadsProfileAndStatus() {
    List<User> users = userRepository.findAll();

    assertThat(users).hasSize(1);
    User user = users.get(0);
    assertThat(user.getUsername()).isEqualTo("test");
    assertThat(user.getProfile()).isNotNull();
    assertThat(user.getProfile().getFileName()).isEqualTo("test.png");
    assertThat(user.getStatus()).isNotNull();
  }

  @Test
  @DisplayName("사용자가 없는 경우 전체를 조회하면 빈 목록을 반환")
  void findAll_noUser_returnEmptyList() {
    em.getEntityManager().createQuery("DELETE FROM UserStatus").executeUpdate();
    em.getEntityManager().createQuery("DELETE FROM User").executeUpdate();

    List<User> users = userRepository.findAll();

    assertThat(users).isEmpty();
  }

  // --- findByEmail ---
  @Test
  @DisplayName("존재하는 이메일로 조회를 할 경우 해당 사용자를 반환")
  void findByEmail_existingEmail_returnsUser() {
    Optional<User> result = userRepository.findByEmail("test@test.com");

    assertThat(result).isPresent();
    assertThat(result.get().getUsername()).isEqualTo("test");
  }

  @Test
  @DisplayName("존재하지 않는 이메일로 조회를 할 경우 빈 Optional을 반환")
  void findByEmail_nonExistingEmail_returnsEmpty() {
    Optional<User> result = userRepository.findByEmail("fake@test.com");

    assertThat(result).isEmpty();
  }

  // --- findByUsername ---
  @Test
  @DisplayName("존재하는 유저이름으로 조회를 할 경우 사용자와 프로필을 함께 반환")
  void findByUsername_existingUsername_returnsUserWithProfile() {
    Optional<User> result = userRepository.findByUsername("test");

    assertThat(result).isPresent();
    assertThat(result.get().getEmail()).isEqualTo("test@test.com");
    assertThat(result.get().getProfile()).isNotNull();
  }

  @Test
  @DisplayName("존재하지 않는 유저명으로 조회를 할 경우 빈 Optional을 반환")
  void findByUsername_nonExistingUsername_returnsEmpty() {
    Optional<User> result = userRepository.findByUsername("fake");

    assertThat(result).isEmpty();
  }
}