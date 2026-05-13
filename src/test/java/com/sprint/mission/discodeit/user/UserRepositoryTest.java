package com.sprint.mission.discodeit.user;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
//@EnableJpaAuditing
public class UserRepositoryTest {

  @Autowired
  private UserRepository userRepository;

  @Test
  @DisplayName("이메일로 사용자 존재 여부 확인 - 존재하는 경우")
  void existsByUsernameOrEmail_exists() {
    //given
    User user = new User("마크", "mark@test.com", "pass1234", null);
    userRepository.save(user);

    //when
    boolean result = userRepository.existsByUsernameOrEmail("마크", "mark@test.com");

    //then
    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("이메일로 사용자 존재 여부 확인 - 존재하지 않는 경우")
  void existsByUsernameOrEmail_not_exists() {
    //given
    User user = new User("마크", "mark@test.com", "pass1234", null);
    userRepository.save(user);

    //when
    boolean result = userRepository.existsByUsernameOrEmail("민형", "mh@test.com");

    //then
    assertThat(result).isFalse();
  }
}
