package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.config.JpaAuditingConfig;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
class ChannelRepositoryTest {

    @Autowired private TestEntityManager em;
    @Autowired private ChannelRepository channelRepository;

    private Channel publicChannel;
    private Channel privateChannel;

    @BeforeEach
    void setUp() {
        publicChannel = Channel.publicChannel("일반", "일반 채널");
        privateChannel = Channel.privateChannel();
        em.persistAndFlush(publicChannel);
        em.persistAndFlush(privateChannel);
    }

    @Test
    void findById_공개채널_반환() {
        Optional<Channel> result = channelRepository.findById(publicChannel.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getType()).isEqualTo(ChannelType.PUBLIC);
        assertThat(result.get().getName()).isEqualTo("일반");
    }

    @Test
    void findById_비공개채널_반환() {
        Optional<Channel> result = channelRepository.findById(privateChannel.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getType()).isEqualTo(ChannelType.PRIVATE);
    }

    @Test
    void findById_존재하지_않는_채널_빈값_반환() {
        Optional<Channel> result = channelRepository.findById(UUID.randomUUID());

        assertThat(result).isEmpty();
    }

    @Test
    void findAll_저장된_채널_목록_반환() {
        List<Channel> result = channelRepository.findAll();

        assertThat(result).hasSize(2);
    }

    @Test
    void findAll_공개채널만_포함_확인() {
        List<Channel> result = channelRepository.findAll();

        long publicCount = result.stream()
                .filter(c -> c.getType() == ChannelType.PUBLIC)
                .count();
        assertThat(publicCount).isEqualTo(1);
    }
}
