package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

@EnableJpaAuditing
@ActiveProfiles("test")
@DataJpaTest
class MessageRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private MessageRepository messageRepository;

  private User author;
  private Channel channel;
  private Channel otherChannel;
  private Message olderMessage;
  private Message newerMessage;

  @BeforeEach
  void setUp() {
    author = new User("tester", "tester@example.io", "qwerty", null);
    entityManager.persist(author);

    channel = new Channel("general", "desc");
    entityManager.persist(channel);

    otherChannel = new Channel("other", "desc");
    entityManager.persist(otherChannel);

    olderMessage = new Message("older", channel, author, List.of());
    entityManager.persistAndFlush(olderMessage);

    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    newerMessage = new Message("newer", channel, author, List.of());
    entityManager.persistAndFlush(newerMessage);
  }

  @Nested
  @DisplayName("find all by channel id order by created at desc")
  class FindAllByChannelIdOrderByCreatedAtDesc {

    @Test
    @DisplayName("return messages in descending order")
    void findAllByChannelIdOrderByCreatedAtDesc_ReturnsMessagesInDescOrder() {
      // given
      Pageable pageable = PageRequest.of(0, 10);

      // when
      Slice<Message> foundMessages = messageRepository.findAllByChannelIdOrderByCreatedAtDesc(
          channel.getId(), pageable);

      // then
      assertThat(foundMessages.getContent()).hasSize(2);
      assertThat(foundMessages.getContent().get(0).getId()).isEqualTo(newerMessage.getId());
      assertThat(foundMessages.getContent().get(1).getId()).isEqualTo(olderMessage.getId());
    }

    @Test
    @DisplayName("exclude messages from other channels")
    void findAllByChannelIdOrderByCreatedAtDesc_ExcludesOtherChannelMessages() {
      // given
      entityManager.persistAndFlush(new Message("other", otherChannel, author, List.of()));
      Pageable pageable = PageRequest.of(0, 10);

      // when
      Slice<Message> foundMessages = messageRepository.findAllByChannelIdOrderByCreatedAtDesc(
          channel.getId(), pageable);

      // then
      assertThat(foundMessages.getContent())
          .allMatch(m -> m.getChannel().getId().equals(channel.getId()));
    }
  }

  @Nested
  @DisplayName("find all by channel id and created at before order by created at desc")
  class FindAllByChannelIdAndCreatedAtBeforeOrderByCreatedAtDesc {

    @Test
    @DisplayName("return messages before cursor")
    void findAllByChannelIdAndCreatedAtBeforeOrderByCreatedAtDesc_ReturnsMessageBeforeCursor() {
      // given
      Instant cursor = olderMessage.getCreatedAt().plusMillis(5);
      Pageable pageable = PageRequest.of(0, 10);

      // when
      Slice<Message> foundMessages = messageRepository
          .findAllByChannelIdAndCreatedAtBeforeOrderByCreatedAtDesc(
              channel.getId(), cursor, pageable);

      // then
      assertThat(foundMessages.getContent()).hasSize(1);
      assertThat(foundMessages.getContent().get(0).getId()).isEqualTo(olderMessage.getId());
    }
  }

  @Nested
  @DisplayName("delete all by channel")
  class DeleteAllByChannel {

    @Test
    @DisplayName("delete all messages in channel")
    void deleteAllByChannel_DeletesAllMessagesInChannel() {
      // when
      messageRepository.deleteAllByChannel(channel);
      entityManager.flush();
      entityManager.clear();

      Slice<Message> foundMessages = messageRepository.findAllByChannelIdOrderByCreatedAtDesc(
          channel.getId(), PageRequest.of(0, 10));

      // then
      assertThat(foundMessages.getContent()).isEmpty();
    }
  }

  @Nested
  @DisplayName("find top created at by channel order by created at desc")
  class FindTopCreatedAtByChannelOrderByCreatedAtDesc {

    @Test
    @DisplayName("return latest created at in channel")
    void findTopCreatedAtByChannelOrderByCreatedAtDesc_ReturnsLatestCreatedAt() {
      // when
      Optional<Instant> foundCreatedAt = messageRepository
          .findTopCreatedAtByChannelOrderByCreatedAtDesc(channel);

      // then
      assertThat(foundCreatedAt).isPresent();
      assertThat(foundCreatedAt.get()).isEqualTo(newerMessage.getCreatedAt());
    }

    @Test
    @DisplayName("return empty optional with no messages in channel")
    void findTopCreatedAtByChannelOrderByCreatedAtDesc_NoMessages_ReturnsEmpty() {
      // when
      Optional<Instant> foundCreatedAt = messageRepository
          .findTopCreatedAtByChannelOrderByCreatedAtDesc(otherChannel);

      // then
      assertThat(foundCreatedAt).isEmpty();
    }
  }

  @Nested
  @DisplayName("find last message at by channel ids")
  class FindLastMessageAtByChannelIds {

    @Test
    @DisplayName("return last message at per channel")
    void findLastMessageAtByChannelIds_ReturnsLastMessageAtPerChannel() {
      // given
      entityManager.persistAndFlush(new Message("hi", otherChannel, author, List.of()));
      entityManager.clear();

      // when
      List<ChannelResponse.LastMessageAt> foundLastMessageAts = messageRepository
          .findLastMessageAtByChannelIds(List.of(channel.getId(), otherChannel.getId()));

      // then
      assertThat(foundLastMessageAts).hasSize(2);
      assertThat(foundLastMessageAts).extracting(ChannelResponse.LastMessageAt::getChannelId)
          .containsExactlyInAnyOrder(channel.getId(), otherChannel.getId());
    }

    @Test
    @DisplayName("return empty list for channels with no messages")
    void findLastMessageAtByChannelIds_NoMessages_ReturnsEmpty() {
      // when
      List<ChannelResponse.LastMessageAt> foundLastMessageAts = messageRepository
          .findLastMessageAtByChannelIds(List.of(otherChannel.getId()));

      // then
      assertThat(foundLastMessageAts).isEmpty();
    }
  }
}