package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.config.JpaAuditingConfig;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaAuditingConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MessageRepositoryTest {

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ChannelRepository channelRepository;

    @Autowired
    BinaryContentRepository binaryContentRepository;

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("채널 ID 목록으로 각 채널의 마지막 메시지 시간을 조회할 수 있다")
    void findLastMessageTimesByChannelIds_success() throws InterruptedException {
        // given
        User author = userRepository.saveAndFlush(
                new User("evan", "evan@test.com", "password123")
        );

        Channel channel = channelRepository.saveAndFlush(
                new Channel("general", "general channel")
        );

        Instant oldTime = Instant.parse("2026-05-09T10:00:00Z");
        Instant newTime = Instant.parse("2026-05-09T11:00:00Z");

        Message oldMessage = messageRepository.saveAndFlush(
                new Message(author, channel, "old message")
        );

        Message newMessage = messageRepository.saveAndFlush(
                new Message(author, channel, "new message")
        );

        updateMessageCreatedAt(oldMessage.getId(), oldTime);
        updateMessageCreatedAt(newMessage.getId(), newTime);

        // when
        List<Object[]> result = messageRepository.findLastMessageTimesByChannelIds(
                List.of(channel.getId())
        );

        // then
        assertThat(result).hasSize(1);

        Object[] row = result.get(0);
        assertThat(row[0]).isEqualTo(channel.getId());
        assertThat(row[1]).isEqualTo(newMessage.getCreatedAt());
        assertThat(newMessage.getCreatedAt()).isAfterOrEqualTo(oldMessage.getCreatedAt());
    }

    @Test
    @DisplayName("커서 없이 채널의 메시지 ID 목록을 최신순으로 조회할 수 있다")
    void findPageIdsByChannelIdAndCursor_withoutCursor() throws InterruptedException {
        // given
        User author = userRepository.saveAndFlush(
                new User("evan", "evan@test.com", "password123")
        );

        Channel channel = channelRepository.saveAndFlush(
                new Channel("general", "general channel")
        );

        Message oldMessage = messageRepository.saveAndFlush(
                new Message(author, channel, "old message")
        );

        Thread.sleep(10);

        Message newMessage = messageRepository.saveAndFlush(
                new Message(author, channel, "new message")
        );

        // when
        List<UUID> result = messageRepository.findPageIdsByChannelIdAndCursor(
                channel.getId(),
                null,
                null,
                PageRequest.of(0, 10)
        );

        // then
        assertThat(result).containsExactly(
                newMessage.getId(),
                oldMessage.getId()
        );
    }

    @Test
    @DisplayName("커서 기준으로 이전 메시지만 조회할 수 있다")
    void findPageIdsByChannelIdAndCursor_withCursor() throws InterruptedException {
        // given
        User author = userRepository.saveAndFlush(
                new User("evan", "evan@test.com", "password123")
        );

        Channel channel = channelRepository.saveAndFlush(
                new Channel("general", "general channel")
        );

        Instant oldTime = Instant.parse("2026-05-09T10:00:00Z");
        Instant newTime = Instant.parse("2026-05-09T11:00:00Z");

        Message oldMessage = messageRepository.saveAndFlush(
                new Message(author, channel, "old message")
        );

        Message newMessage = messageRepository.saveAndFlush(
                new Message(author, channel, "new message")
        );

        updateMessageCreatedAt(oldMessage.getId(), oldTime);
        updateMessageCreatedAt(newMessage.getId(), newTime);

        Instant cursorCreatedAt = newTime;
        UUID cursorId = newMessage.getId();

        // when
        List<UUID> result = messageRepository.findPageIdsByChannelIdAndCursor(
                channel.getId(),
                cursorCreatedAt,
                cursorId,
                PageRequest.of(0, 10)
        );

        // then
        assertThat(result).containsExactly(oldMessage.getId());
    }

    @Test
    @DisplayName("메시지 ID 목록으로 작성자와 첨부파일을 함께 조회할 수 있다")
    void findAllWithDetailsByIdIn_success() {
        // given
        User author = userRepository.saveAndFlush(
                new User("evan", "evan@test.com", "password123")
        );

        Channel channel = channelRepository.saveAndFlush(
                new Channel("general", "general channel")
        );

        BinaryContent attachment = binaryContentRepository.saveAndFlush(
                new BinaryContent("image.png", 100L, "image/png")
        );

        Message message = new Message(author, channel, "message with attachment");
        message.updateAttachments(List.of(attachment));

        Message savedMessage = messageRepository.saveAndFlush(message);

        entityManager.flush();
        entityManager.clear();

        // when
        List<Message> result = messageRepository.findAllWithDetailsByIdIn(
                List.of(savedMessage.getId())
        );

        // then
        assertThat(result).hasSize(1);

        Message foundMessage = result.get(0);

        assertThat(foundMessage.getId()).isEqualTo(savedMessage.getId());
        assertThat(foundMessage.getAuthor().getUsername()).isEqualTo("evan");
        assertThat(foundMessage.getAttachments()).hasSize(1);
        assertThat(foundMessage.getAttachments().get(0).getFileName()).isEqualTo("image.png");
    }

    private void updateMessageCreatedAt(UUID messageId, Instant createdAt) {
        jdbcTemplate.update(
                "update messages set created_at = ?, updated_at = ? where id = ?",
                Timestamp.from(createdAt),
                Timestamp.from(createdAt),
                messageId
        );

        entityManager.flush();
        entityManager.clear();
    }
}