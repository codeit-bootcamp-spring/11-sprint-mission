package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.TestJpaConfig;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import java.util.List;
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
class ChannelRepositoryTest {

    @Autowired
    TestEntityManager em;

    @Autowired
    ChannelRepository channelRepository;

    private Channel publicChannel;
    private Channel privateChannel;

    @BeforeEach
    void setUp() {
        publicChannel = em.persistAndFlush(new Channel(ChannelType.PUBLIC, "general", "공개채널"));
        privateChannel = em.persistAndFlush(new Channel(ChannelType.PRIVATE, null, null));
        em.clear();
    }

    @Test
    void findAllByTypeOrIdIn_PUBLIC채널_전체반환() {
        List<Channel> result = channelRepository.findAllByTypeOrIdIn(ChannelType.PUBLIC, List.of());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo(ChannelType.PUBLIC);
    }

    @Test
    void findAllByTypeOrIdIn_PRIVATE채널_ID로조회() {
        List<Channel> result = channelRepository.findAllByTypeOrIdIn(
            ChannelType.PUBLIC, List.of(privateChannel.getId())
        );

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Channel::getId)
            .containsExactlyInAnyOrder(publicChannel.getId(), privateChannel.getId());
    }

    @Test
    void findAllByTypeOrIdIn_없는ID_PUBLIC만반환() {
        List<Channel> result = channelRepository.findAllByTypeOrIdIn(
            ChannelType.PUBLIC, List.of(UUID.randomUUID())
        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo(ChannelType.PUBLIC);
    }
}