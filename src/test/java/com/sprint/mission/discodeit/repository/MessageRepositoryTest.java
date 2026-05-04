package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@EnableJpaAuditing
@AutoConfigureTestDatabase(replace = Replace.NONE)
class MessageRepositoryTest {

  @Autowired
  private MessageRepository messageRepository;

  @Autowired
  private TestEntityManager em;

  private Channel channel;
  private User author;
  private Message oldMessage;
  private Message newMessage;

  @BeforeEach
  void setUp() throws InterruptedException {
    author = em.persistAndFlush(new User("test", "test@test.com", "password123", null));
    channel = em.persistAndFlush(new Channel("public", "public channel", ChannelType.PUBLIC));

    oldMessage = em.persistAndFlush(new Message(channel, author, "첫 메시지"));
    Thread.sleep(10);
    newMessage = em.persistAndFlush(new Message(channel, author, "두번째 메시지"));

    channel.updateLastMessageAt(newMessage.getCreatedAt());
    em.flush();
    em.clear();
  }

  // --- findLastestMessageByChannelIds ---
  @Test
  @DisplayName("채널 ID 목록으로 각 채널의 최신 메시지를 조회")
  void findLatestMessagesByChannelIds_existingChannels_returnsLatestMessages() {
    List<Message> result = messageRepository.findLatestMessagesByChannelIds(
        List.of(channel.getId())
    );

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getContent()).isEqualTo("두번째 메시지");
  }

  @Test
  @DisplayName("존재하지 않는 채널 ID로 조회 할 경우 빈 목록을 반환")
  void findLatestMessagesByChannelIds_nonExistingChannel_returnsEmpty() {
    List<Message> result = messageRepository.findLatestMessagesByChannelIds(
        List.of(UUID.randomUUID())
    );

    assertThat(result).isEmpty();
  }

  // --- findTopByChannelIdOrderByCreatedAtDesc ---
  @Test
  @DisplayName("채널에서 가장 최근에 생성된 메시지를 반환")
  void findTopByChannelIdOrderByCreatedAtDesc_existingChannel_returnsLatestMessage() {
    Optional<Message> result = messageRepository
        .findTopByChannelIdOrderByCreatedAtDesc(channel.getId());

    assertThat(result).isPresent();
    assertThat(result.get().getContent()).isEqualTo("두번째 메시지");
  }

  @Test
  @DisplayName("존재하지 않는 채널 ID로 조회 할 경우 빈 Optional을 반환")
  void findTopByChannelIdOrderByCreatedAtDesc_nonExistentChannel_returnsEmpty() {
    Optional<Message> result = messageRepository
        .findTopByChannelIdOrderByCreatedAtDesc(UUID.randomUUID());

    assertThat(result).isEmpty();
  }

  // --- findByChannelIdOrderByCreatedAtDesc ---
  @Test
  @DisplayName("Limit 1일 경우 채널의 최신 메시지 하나만 반환")
  void findByChannelIdOrderByCreatedAtDesc_withLimit1_returnsOnlyLatest() {
    List<Message> result = messageRepository
        .findByChannelIdOrderByCreatedAtDesc(channel.getId(), Limit.of(1));

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getContent()).isEqualTo("두번째 메시지");
  }

  @Test
  @DisplayName("Limit이 메시지 수보다 클 경우 전체 메시지를 최신순으로 반환")
  void findByChannelIdOrderByCreatedAtDesc_withLargeLimit_returnsAllSorted() {
    List<Message> result = messageRepository
        .findByChannelIdOrderByCreatedAtDesc(channel.getId(), Limit.of(100));

    assertThat(result).hasSize(2);
    assertThat(result.get(0).getContent()).isEqualTo("두번째 메시지");
    assertThat(result.get(1).getContent()).isEqualTo("첫 메시지");
  }

  // --- findByChannelIdAndCreatedAtBeforeOrderByCreatedAtDesc ---
  @Test
  @DisplayName("새로 생성된 메시지 이전 메시지를 최신순으로 반환")
  void findByChannelIdAndCreatedAtBeforeOrderByCreatedAtDesc_returnsPreviousMessages() {
    List<Message> result = messageRepository
        .findByChannelIdAndCreatedAtBeforeOrderByCreatedAtDesc(
            channel.getId(),
            newMessage.getCreatedAt(),
            Limit.of(10)
        );

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getContent()).isEqualTo("첫 메시지");
  }

  @Test
  @DisplayName("가장 오래된 메시지의 createdAt과 같은경우 빈 목록을 반환")
  void findByChannelIdAndCreatedAtBeforeOrderByCreatedAtDesc_returnsEmpty() {
    List<Message> result = messageRepository
        .findByChannelIdAndCreatedAtBeforeOrderByCreatedAtDesc(
            channel.getId(),
            oldMessage.getCreatedAt(),
            Limit.of(10)
        );

    assertThat(result).isEmpty();
  }
}