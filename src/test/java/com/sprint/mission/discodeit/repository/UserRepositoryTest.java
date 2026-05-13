package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.entity.User;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

  @Autowired
  private UserRepository userRepository;

  // ───── existsByUsername ─────

  @Test
  void existsByUsername_존재하는경우_true() {
    // given
    User user = new User("jihye", "jihye@test.com", "password123", null);
    userRepository.save(user);

    // when
    boolean result = userRepository.existsByUsername("jihye");

    // then
    assertThat(result).isTrue();
  }

  @Test
  void existsByUsername_존재하지않는경우_false() {
    // when
    boolean result = userRepository.existsByUsername("notexist");

    // then
    assertThat(result).isFalse();
  }

  // ───── existsByEmail ─────

  @Test
  void existsByEmail_존재하는경우_true() {
    // given
    User user = new User("jihye", "jihye@test.com", "password123", null);
    userRepository.save(user);

    // when
    boolean result = userRepository.existsByEmail("jihye@test.com");

    // then
    assertThat(result).isTrue();
  }

  @Test
  void existsByEmail_존재하지않는경우_false() {
    // when
    boolean result = userRepository.existsByEmail("notexist@test.com");

    // then
    assertThat(result).isFalse();
  }

  // ───── findByUsername ─────

  @Test
  void findByUsername_존재하는경우_반환() {
    // given
    User user = new User("jihye", "jihye@test.com", "password123", null);
    userRepository.save(user);

    // when
    Optional<User> result = userRepository.findByUsername("jihye");

    // then
    assertThat(result).isPresent();
    assertThat(result.get().getUsername()).isEqualTo("jihye");
  }

  @Test
  void findByUsername_존재하지않는경우_빈Optional() {
    // when
    Optional<User> result = userRepository.findByUsername("notexist");

    // then
    assertThat(result).isEmpty();
  }

  // ───── 페이징 ─────

  @Test
  void findAll_페이징_정렬() {
    // given
    userRepository.save(new User("aaa", "aaa@test.com", "password123", null));
    userRepository.save(new User("bbb", "bbb@test.com", "password123", null));
    userRepository.save(new User("ccc", "ccc@test.com", "password123", null));

    // when
    List<User> result = userRepository.findAll(
        PageRequest.of(0, 2, Sort.by("username").ascending())
    ).getContent();

    // then
    assertThat(result).hasSize(2);
    assertThat(result.get(0).getUsername()).isEqualTo("aaa");
  }

  @Test
  void findAll_페이징_두번째페이지() {
    // given
    userRepository.save(new User("aaa", "aaa@test.com", "password123", null));
    userRepository.save(new User("bbb", "bbb@test.com", "password123", null));
    userRepository.save(new User("ccc", "ccc@test.com", "password123", null));

    // when
    List<User> result = userRepository.findAll(
        PageRequest.of(1, 2, Sort.by("username").ascending())
    ).getContent();

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getUsername()).isEqualTo("ccc");
  }
}