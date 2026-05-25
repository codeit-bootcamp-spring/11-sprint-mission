package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.config.JpaAuditingConfig;
import com.sprint.mission.discodeit.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaAuditingConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Test
    @DisplayName("username으로 사용자를 조회할 수 있다")
    void findByUsername_success() {
        // given
        User user = new User("evan", "evan@test.com", "password123");
        userRepository.saveAndFlush(user);

        // when
        Optional<User> result = userRepository.findByUsername("evan");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("evan");
        assertThat(result.get().getEmail()).isEqualTo("evan@test.com");
    }

    @Test
    @DisplayName("존재하지 않는 username으로 조회하면 Optional.empty를 반환한다")
    void findByUsername_empty() {
        // when
        Optional<User> result = userRepository.findByUsername("not-exist");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("username 중복 여부를 확인할 수 있다")
    void existsByUsername_true() {
        // given
        User user = new User("evan", "evan@test.com", "password123");
        userRepository.saveAndFlush(user);

        // when
        boolean exists = userRepository.existsByUsername("evan");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("없는 email은 중복되지 않은 것으로 확인된다")
    void existsByEmail_false() {
        // when
        boolean exists = userRepository.existsByEmail("none@test.com");

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("username과 password로 로그인 사용자를 조회할 수 있다")
    void findByUsernameAndPassword_success() {
        // given
        User user = new User("evan", "evan@test.com", "password123");
        userRepository.saveAndFlush(user);

        // when
        Optional<User> result = userRepository.findByUsernameAndPassword("evan", "password123");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("evan@test.com");
    }

    @Test
    @DisplayName("password가 틀리면 로그인 사용자를 조회하지 못한다")
    void findByUsernameAndPassword_empty_whenPasswordWrong() {
        // given
        User user = new User("evan", "evan@test.com", "password123");
        userRepository.saveAndFlush(user);

        // when
        Optional<User> result = userRepository.findByUsernameAndPassword("evan", "wrong-password");

        // then
        assertThat(result).isEmpty();
    }
}