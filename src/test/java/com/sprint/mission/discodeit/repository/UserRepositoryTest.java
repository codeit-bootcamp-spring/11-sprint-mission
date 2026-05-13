package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.TestJpaConfig;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import(TestJpaConfig.class)
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    TestEntityManager em;

    @Autowired
    UserRepository userRepository;

    private User savedUser;

    @BeforeEach
    void setUp() {
        User user = new User("testuser", "test@test.com", "password", null);
        em.persist(user);
        em.persist(new UserStatus(user, Instant.now()));
        em.flush();
        em.clear();
        savedUser = user;
    }

    @Test
    void findByUsername_존재하는경우_반환() {
        Optional<User> result = userRepository.findByUsername("testuser");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@test.com");
    }

    @Test
    void findByUsername_없는경우_빈Optional() {
        Optional<User> result = userRepository.findByUsername("nonexistent");

        assertThat(result).isEmpty();
    }

    @Test
    void existsByEmail_존재하면_true() {
        assertThat(userRepository.existsByEmail("test@test.com")).isTrue();
    }

    @Test
    void existsByEmail_없으면_false() {
        assertThat(userRepository.existsByEmail("other@test.com")).isFalse();
    }

    @Test
    void existsByUsername_존재하면_true() {
        assertThat(userRepository.existsByUsername("testuser")).isTrue();
    }

    @Test
    void existsByUsername_없으면_false() {
        assertThat(userRepository.existsByUsername("otheruser")).isFalse();
    }

    @Test
    void findAllWithProfileAndStatus_status있는유저만반환() {
        List<User> result = userRepository.findAllWithProfileAndStatus();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("testuser");
        assertThat(result.get(0).getStatus()).isNotNull();
    }

    @Test
    void findAllWithProfileAndStatus_status없는유저는미포함() {
        User noStatusUser = new User("nostatususer", "nostatus@test.com", "password", null);
        em.persistAndFlush(noStatusUser);

        List<User> result = userRepository.findAllWithProfileAndStatus();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("testuser");
    }
}