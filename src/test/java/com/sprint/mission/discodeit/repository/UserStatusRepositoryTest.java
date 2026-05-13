package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.TestJpaConfig;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
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
class UserStatusRepositoryTest {

    @Autowired
    TestEntityManager em;

    @Autowired
    UserStatusRepository userStatusRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("testuser", "test@test.com", "password", null);
        em.persist(user);
        em.persist(new UserStatus(user, Instant.now()));
        em.flush();
        em.clear();
    }

    @Test
    void findByUserId_존재하면_반환() {
        Optional<UserStatus> result = userStatusRepository.findByUserId(user.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    void findByUserId_없으면_빈Optional() {
        Optional<UserStatus> result = userStatusRepository.findByUserId(UUID.randomUUID());

        assertThat(result).isEmpty();
    }
}