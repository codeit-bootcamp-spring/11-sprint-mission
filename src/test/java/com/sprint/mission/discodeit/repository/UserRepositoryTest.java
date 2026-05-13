package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.entity.User;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@EnableJpaAuditing
public class UserRepositoryTest {

  @Autowired
  private UserRepository userRepository;

  @Test
  @DisplayName("username으로 유저 존재 여부 확인 - 존재하는 경우")
  void existsByUsername_exists() {
    User user = new User("testUser", "test@email.com", "password132");
    userRepository.save(user);

    boolean exists = userRepository.existsByUsername("testUser");

    assertThat(exists).isTrue();
  }

  @Test
  @DisplayName("username으로 유저 존재 여부 확인 - 존재하지 않는 경우")
  void existsByUsername_notExists() {
    boolean notExists = userRepository.existsByUsername("unknownUser");

    assertThat(notExists).isFalse();
  }

  @Test
  @DisplayName("email로 유저 존재 여부 확인 - 존재하는 경우")
  void existsByEmail_exists() {
    User user = new User("testUser", "test@email.com", "password132");
    userRepository.save(user);

    boolean exists = userRepository.existsByEmail("test@email.com");

    assertThat(exists).isTrue();
  }

  @Test
  @DisplayName("email로 유저 존재 여부 확인 - 존재하지 않는 경우")
  void existsByEmail_notExists() {
    boolean notExists = userRepository.existsByEmail("unknown@email.com");

    assertThat(notExists).isFalse();
  }

  @Test
  @DisplayName("username으로 유저 조회 - 존재하는 경우")
  void findByUsername_found() {
    User user = new User("testUser", "test@email.com", "password132");
    userRepository.save(user);

    Optional<User> foundUser = userRepository.findByUsername("testUser");

    assertThat(foundUser).isPresent();
    assertThat(foundUser.get().getEmail()).isEqualTo("test@email.com");
  }

  @Test
  @DisplayName("username으로 유저 조회 - 존재하지 않는 경우")
  void findByUsername_notFound() {
    Optional<User> foundUser = userRepository.findByUsername("unknownUser");

    assertThat(foundUser).isEmpty();
  }
}
