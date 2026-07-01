package com.sprint.mission.discodeit.repository;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.tuple;

import com.sprint.mission.discodeit.entity.User;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EnableJpaAuditing
@Transactional
public class UserRepositoryTest {

  @Autowired
  private JPAUserRepository userRepository;

  @BeforeEach
  void setUp() {
    User user1 = new User(
        "테스트1",
        "test1@test.com",
        "testpassword1",
        null,
        null

    );
    User user2 = new User(
        "테스트2",
        "test2@test.com",
        "testpassword2",
        null,
        null

    );

    UserStatus status1 = new UserStatus(user1, Instant.now());
    UserStatus status2 = new UserStatus(user2, Instant.now());
    user1.updateStatus(status1);
    user2.updateStatus(status2);
    userRepository.save(user1);
    userRepository.save(user2);
  }


  @Test
  @DisplayName("전체 유저 가져오기 요청")
  void testFindAllTest() {
    //given & when
    List<User> result = userRepository.findAll();

    //then

    assertThat(result).isNotNull();
    assertThat(result).hasSize(2);

    assertThat(result)
        .extracting("username", "email")
        .containsExactlyInAnyOrder(
            tuple("테스트1", "test1@test.com"),
            tuple("테스트2", "test2@test.com")
        );

  }


  @Test
  @DisplayName("유저 이름으로 찾기 테스트")
  void findByUsernameTest() {

    //given & when

    Optional<User> result = userRepository.findByUsername("테스트1");

    //then

    assertThat(result.isPresent()).isTrue();
    assertThat(result.get().getEmail()).isEqualTo("test1@test.com");


  }

  @Test
  @DisplayName("없는 유저 이름으로 찾기 테스트")
  void findByNoneUsernameTest() {

    //given & when

    Optional<User> result = userRepository.findByUsername("테스트3");

    //then

    assertThat(result.isEmpty()).isTrue();


  }

  @Test
  @DisplayName("유저 이름으로 존재여부 테스트")
  void ExistByUsernameTest() {

    //given & when

    Boolean result = userRepository.existsByUsername("테스트1");

    //then

    assertThat(result).isTrue();


  }

  @Test
  @DisplayName("없는 유저 이름으로 존재여부 테스트")
  void ExistByNonUsernameTest() {

    //given & when

    Boolean result = userRepository.existsByUsername("테스트3");

    //then

    assertThat(result).isFalse();


  }

  @Test
  @DisplayName("이메일으로 존재여부 테스트")
  void ExistByEmailTest() {

    //given & when

    Boolean result = userRepository.existsByEmail("test1@test.com");

    //then

    assertThat(result).isTrue();


  }

  @Test
  @DisplayName("없는 이메일으로 존재여부 테스트")
  void ExistByNonEmailTest() {

    //given & when

    Boolean result = userRepository.existsByEmail("test3@test.com");

    //then

    assertThat(result).isFalse();


  }


}
