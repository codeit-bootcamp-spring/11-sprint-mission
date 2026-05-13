package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.config.JpaAuditingConfig;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaAuditingConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MessageRepositoryTest {

    @Autowired private TestEntityManager em;
    @Autowired private MessageRepository messageRepository;

    private User user;
    private Channel channel;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password1234")
                .build();
        channel = Channel.publicChannel("일반", "일반 채널");
        em.persistAndFlush(user);
        em.persistAndFlush(channel);
    }

    @Test
    void findAllByChannelIdOrderByCreatedAtDesc_메시지_목록_반환() {
        em.persistAndFlush(new Message(user, channel, "첫 번째 메시지"));
        em.persistAndFlush(new Message(user, channel, "두 번째 메시지"));

        Slice<Message> result = messageRepository
                .findAllByChannelIdOrderByCreatedAtDesc(channel.getId(), PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void findAllByChannelIdOrderByCreatedAtDesc_다른채널_메시지_제외() {
        Channel otherChannel = Channel.publicChannel("다른채널", null);
        em.persistAndFlush(otherChannel);
        em.persistAndFlush(new Message(user, channel, "내 채널 메시지"));
        em.persistAndFlush(new Message(user, otherChannel, "다른 채널 메시지"));

        Slice<Message> result = messageRepository
                .findAllByChannelIdOrderByCreatedAtDesc(channel.getId(), PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getContent()).isEqualTo("내 채널 메시지");
    }

    @Test
    void findAllByChannelIdOrderByCreatedAtDesc_페이지_크기_제한() {
        for (int i = 1; i <= 5; i++) {
            em.persistAndFlush(new Message(user, channel, "메시지 " + i));
        }

        Slice<Message> result = messageRepository
                .findAllByChannelIdOrderByCreatedAtDesc(channel.getId(), PageRequest.of(0, 3));

        assertThat(result.getContent()).hasSize(3);
        assertThat(result.hasNext()).isTrue();
    }

    @Test
    void findAllByChannelIdAndCreatedAtBeforeOrderByCreatedAtDesc_미래_커서_전체_반환() {
        em.persistAndFlush(new Message(user, channel, "메시지 1"));
        em.persistAndFlush(new Message(user, channel, "메시지 2"));

        Instant futureCursor = Instant.now().plusSeconds(3600);
        Slice<Message> result = messageRepository
                .findAllByChannelIdAndCreatedAtBeforeOrderByCreatedAtDesc(
                        channel.getId(), futureCursor, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void findAllByChannelIdAndCreatedAtBeforeOrderByCreatedAtDesc_과거_커서_빈값_반환() {
        em.persistAndFlush(new Message(user, channel, "메시지"));

        Instant pastCursor = Instant.EPOCH;
        Slice<Message> result = messageRepository
                .findAllByChannelIdAndCreatedAtBeforeOrderByCreatedAtDesc(
                        channel.getId(), pastCursor, PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void findLatestCreatedAtByChannelId_메시지_있음_최신시각_반환() {
        em.persistAndFlush(new Message(user, channel, "메시지"));

        Optional<Instant> result = messageRepository.findLatestCreatedAtByChannelId(channel.getId());

        assertThat(result).isPresent();
    }

    @Test
    void findLatestCreatedAtByChannelId_메시지_없음_빈값_반환() {
        Optional<Instant> result = messageRepository
                .findLatestCreatedAtByChannelId(UUID.randomUUID());

        assertThat(result).isEmpty();
    }
}
