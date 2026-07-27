package com.sprint.mission.discodeit.slice.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
  @DisplayName("모든 유저 조회 성공")
  void findAll_success() {
    // given
    userRepository.save(User.create("test1", "test1@naver.com", "12345678"));
    userRepository.save(User.create("test2", "test2@naver.com", "1q2w3e4r"));

    // when
    List<User> result = userRepository.findAll();

    // then
    assertThat(result).hasSize(2);
  }

//  @Test
//  @DisplayName("모든 유저 조회 실패")
//  void findAll_fail() {
//    // given : 유저 1명만 생성
//    userRepository.save(User.create("test1", "test1@naver.com", "12345678"));
//
//    // when
//    List<User> result = userRepository.findAll();
//
//    // then : 2명의 유저가 있을거라 추측
//    assertThat(result).hasSize(2);
//  }

  @Test
  @DisplayName("유저ID로 유저 조회 성공")
  void findById_success() {
    // given
    User savedUser = userRepository.save(User.create("test", "test@naver.com", "12345678"));

    // when
    Optional<User> result = userRepository.findById(savedUser.getId());

    // then
    assertThat(result).isPresent();
    assertEquals(savedUser.getId(), result.get().getId());
  }

  @Test
  @DisplayName("유저ID로 유저 조회 실패(유저가 존재하지 않음)")
  void findById_fail_emptyUsers() {
    // given : userId만 생성하여 가짜 유저 생성
    UUID userId = UUID.randomUUID();

    // when : DB에 유저ID가 존재하는가
    Optional<User> result = userRepository.findById(userId);

    // then : false
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("Username 존재 여부 조회 성공")
  void existsByUsername_success() {
    // given
    userRepository.save(User.create("test", "test@naver.com", "12345678"));

    // when
    boolean result = userRepository.existsByUsername("test");

    // then
    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("Username 존재 여부 조회 실패(유저가 존재하지 않음)")
  void existsByUsername_fail_emptyUsers() {
    // given : 유저 생성 X

    // when
    boolean result = userRepository.existsByUsername("test");

    // then
    assertThat(result).isFalse();

  }

  @Test
  @DisplayName("Email 존재 여부 조회 성공")
  void existsByEmail_success() {
    // given
    userRepository.save(User.create("test", "test@naver.com", "12345678"));

    // when
    boolean result = userRepository.existsByEmail("test@naver.com");

    // then
    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("Email 존재 여부 조회 실패(유저가 존재하지 않음)")
  void existsByEmail_fail() {
    // given : test@naver.com 이메일이 존재하지 않도록 유저 생성X

    // when
    boolean result = userRepository.existsByEmail("test@naver.com");

    // then
    assertThat(result).isFalse();
  }

  @Test
  @DisplayName("중복 이름이 존재하는지 조회 성공")
  void existsByUsernameAndIdNot_success() {
    // given
    UUID otherUserId = UUID.randomUUID();
    User my = userRepository.save(User.create("test", "test@naver.com", "12345678"));

    // when : otherUser외의 User가 test를 가지고 있는지
    boolean result = userRepository.existsByUsernameAndIdNot("test", otherUserId);

    // then
    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("중복 이름이 존재하는지 조회 실패(my라는 User만 test라는 Username을 가지고 있음")
  void existsByUsernameAndIdNot_fail() {
    // given
    User my = userRepository.save(User.create("test", "test@naver.com", "12345678"));

    // when : my 외의 User가 test를 가지고 있는지
    boolean result = userRepository.existsByUsernameAndIdNot("test", my.getId());

    // then
    assertThat(result).isFalse();
  }

  @Test
  @DisplayName("중복된 이메일이 존재하는지 조회 성공")
  void existsByEmailAndIdNot_success() {
    // given
    UUID otherUserId = UUID.randomUUID();
    User my = userRepository.save(User.create("test", "test@naver.com", "12345678"));

    // when : otherUser외의 User가 test@naver.com를 가지고 있는지
    boolean result = userRepository.existsByEmailAndIdNot("test@naver.com", otherUserId);

    // then
    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("중복된 이메일이 존재하는지 조회 실패(my라는 User만 test@naver.com email을 가지고 있음")
  void existsByEmailAndIdNot_fail() {
    // given
    User my = userRepository.save(User.create("test", "test@naver.com", "12345678"));

    // when : my 외의 User가 test@naver.com을 가지고 있는지
    boolean result = userRepository.existsByEmailAndIdNot("test@naver.com", my.getId());

    // then
    assertThat(result).isFalse();
  }

  @Test
  @DisplayName("Username으로 유저 조회 성공")
  void findByUsername_success() {
    // given
    User savedUser = userRepository.save(User.create("test", "test@naver.com", "12345678"));

    // when
    Optional<User> result = userRepository.findByUsername("test");

    // then
    assertThat(result).isPresent();
    assertEquals(savedUser.getId(), result.get().getId());
    assertEquals(savedUser.getUsername(), result.get().getUsername());
    assertEquals(savedUser.getEmail(), result.get().getEmail());
  }

  @Test
  @DisplayName("Username으로 조회 실패(존재하지 않는 Username)")
  void findByUsername_fail() {
    // given : 유저를 생성하지 않아 실패 유도
    // userRepository.save(User.create("test", "test@naver.com", "12345678"));

    // when : "test"라는 이름을 가진 유저를 찾을 때
    Optional<User> result = userRepository.findByUsername("test");

    // then : 조회결과 비어있을 경우
    assertThat(result).isEmpty();
  }

}
