package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.TestJpaConfig;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import java.time.Instant;
import java.util.List;
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
class ReadStatusRepositoryTest {

    @Autowired
    TestEntityManager em;

    @Autowired
    ReadStatusRepository readStatusRepository;

    private User user;
    private Channel channel;

    @BeforeEach
    void setUp() {
        user = new User("testuser", "test@test.com", "password", null);
        em.persist(user);
        em.persist(new UserStatus(user, Instant.now()));

        channel = em.persistAndFlush(new Channel(ChannelType.PRIVATE, null, null));

        em.persist(new ReadStatus(user, channel, Instant.now()));
        em.flush();
        em.clear();
    }

    @Test
    void findAllByChannelIdWithUser_채널의ReadStatus목록반환() {
        List<ReadStatus> result = readStatusRepository.findAllByChannelIdWithUser(channel.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUser().getUsername()).isEqualTo("testuser");
        assertThat(result.get(0).getUser().getStatus()).isNotNull();
    }

    @Test
    void findByUserIdAndChannelId_존재하면_반환() {
        Optional<ReadStatus> result = readStatusRepository.findByUserIdAndChannelId(
            user.getId(), channel.getId()
        );

        assertThat(result).isPresent();
        assertThat(result.get().getUser().getId()).isEqualTo(user.getId());
        assertThat(result.get().getChannel().getId()).isEqualTo(channel.getId());
    }

    @Test
    void findByUserIdAndChannelId_없는유저_빈Optional() {
        Optional<ReadStatus> result = readStatusRepository.findByUserIdAndChannelId(
            UUID.randomUUID(), channel.getId()
        );

        assertThat(result).isEmpty();
    }

    @Test
    void findAllByUserId_유저의ReadStatus목록반환() {
        List<ReadStatus> result = readStatusRepository.findAllByUserId(user.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getChannel().getId()).isEqualTo(channel.getId());
    }

    @Test
    void findAllByUserId_없는유저_빈목록() {
        List<ReadStatus> result = readStatusRepository.findAllByUserId(UUID.randomUUID());

        assertThat(result).isEmpty();
    }
}