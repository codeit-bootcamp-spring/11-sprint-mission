package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.TestJpaConfig;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import(TestJpaConfig.class)
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
class MessageRepositoryTest {

    @Autowired
    TestEntityManager em;

    @Autowired
    MessageRepository messageRepository;

    private Channel channel;
    private User author;

    @BeforeEach
    void setUp() {
        author = new User("author", "author@test.com", "password", null);
        em.persist(author);
        em.persist(new UserStatus(author, Instant.now()));

        channel = em.persistAndFlush(new Channel(ChannelType.PUBLIC, "general", null));

        em.persist(new Message("첫 번째 메시지", channel, author, List.of()));
        em.persist(new Message("두 번째 메시지", channel, author, List.of()));
        em.flush();
        em.clear();
    }

    @Test
    void findAllByChannelIdWithAuthor_메시지목록_반환() {
        Slice<Message> result = messageRepository.findAllByChannelIdWithAuthor(
            channel.getId(),
            Instant.now().plusSeconds(5),
            PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(m -> m.getAuthor() != null);
        assertThat(result.getContent()).allMatch(m -> m.getAuthor().getStatus() != null);
    }

    @Test
    void findAllByChannelIdWithAuthor_커서이전메시지만반환() {
        Slice<Message> result = messageRepository.findAllByChannelIdWithAuthor(
            channel.getId(),
            Instant.EPOCH,
            PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void findAllByChannelIdWithAuthor_페이지크기_적용() {
        Slice<Message> result = messageRepository.findAllByChannelIdWithAuthor(
            channel.getId(),
            Instant.now().plusSeconds(5),
            PageRequest.of(0, 1)
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.hasNext()).isTrue();
    }

    @Test
    void findLastMessageAtByChannelId_마지막메시지시간반환() {
        Optional<Instant> result = messageRepository.findLastMessageAtByChannelId(channel.getId());

        assertThat(result).isPresent();
    }

    @Test
    void findLastMessageAtByChannelId_메시지없으면_빈Optional() {
        Channel emptyChannel = em.persistAndFlush(new Channel(ChannelType.PUBLIC, "empty", null));

        Optional<Instant> result = messageRepository.findLastMessageAtByChannelId(emptyChannel.getId());

        assertThat(result).isEmpty();
    }
}