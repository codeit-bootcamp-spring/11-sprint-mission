package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.config.JpaAuditingConfig;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaAuditingConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired private TestEntityManager em;
    @Autowired private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password1234")
                .build();
        em.persistAndFlush(user);
    }

    @Test
    void findByUsername_존재하는_유저명_반환() {
        Optional<User> result = userRepository.findByUsername("testuser");

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("testuser");
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void findByUsername_없는_유저명_빈값_반환() {
        Optional<User> result = userRepository.findByUsername("unknown");

        assertThat(result).isEmpty();
    }

    @Test
    void existsByUsername_존재하는_유저명_true() {
        boolean result = userRepository.existsByUsername("testuser");

        assertThat(result).isTrue();
    }

    @Test
    void existsByUsername_없는_유저명_false() {
        boolean result = userRepository.existsByUsername("unknown");

        assertThat(result).isFalse();
    }

    @Test
    void existsByEmail_존재하는_이메일_true() {
        boolean result = userRepository.existsByEmail("test@example.com");

        assertThat(result).isTrue();
    }

    @Test
    void existsByEmail_없는_이메일_false() {
        boolean result = userRepository.existsByEmail("nobody@example.com");

        assertThat(result).isFalse();
    }

    @Test
    void findAll_저장된_유저_목록_반환() {
        User anotherUser = User.builder()
                .username("another")
                .email("another@example.com")
                .password("pw")
                .build();
        em.persistAndFlush(anotherUser);

        List<User> result = userRepository.findAll();

        assertThat(result).hasSize(2);
    }
}
