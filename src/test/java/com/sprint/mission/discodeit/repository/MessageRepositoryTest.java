package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Channel.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class MessageRepositoryTest {

  @Autowired
  private MessageRepository messageRepository;
  @Autowired
  private ChannelRepository channelRepository;
  @Autowired
  private UserRepository userRepository;

  private Channel channel;
  private User author;

  @BeforeEach
  void setUp() {
    channel = channelRepository.save(new Channel(ChannelType.PUBLIC, "general", "공용 채널"));
    author = userRepository.save(new User("jihye", "jihye@test.com", "password123", null));
  }

  // ───── findAllByChannelIdOrderByCreatedAtDesc ─────

  @Test
  void findAllByChannelId_메시지반환() {
    // given
    messageRepository.save(new Message("첫번째 메시지", channel, author));
    messageRepository.save(new Message("두번째 메시지", channel, author));

    // when
    Slice<Message> result = messageRepository.findAllByChannelIdOrderByCreatedAtDesc(
        channel.getId(), PageRequest.of(0, 10));

    // then
    assertThat(result.getContent()).hasSize(2);
  }

  @Test
  void findAllByChannelId_다른채널_빈목록() {
    // given
    messageRepository.save(new Message("첫번째 메시지", channel, author));
    Channel otherChannel = channelRepository.save(
        new Channel(ChannelType.PUBLIC, "other", "다른 채널"));

    // when
    Slice<Message> result = messageRepository.findAllByChannelIdOrderByCreatedAtDesc(
        otherChannel.getId(), PageRequest.of(0, 10));

    // then
    assertThat(result.getContent()).isEmpty();
  }

  // ───── findTopByChannelIdOrderByCreatedAtDesc ─────

  @Test
  void findTopByChannelId_최신메시지반환() throws InterruptedException {
    // given
    messageRepository.save(new Message("첫번째 메시지", channel, author));
    Thread.sleep(10);
    messageRepository.save(new Message("두번째 메시지", channel, author));

    // when
    Optional<Message> result = messageRepository.findTopByChannelIdOrderByCreatedAtDesc(
        channel.getId());

    // then
    assertThat(result).isPresent();
    assertThat(result.get().getContent()).isEqualTo("두번째 메시지");
  }

  @Test
  void findTopByChannelId_메시지없는경우_빈Optional() {
    // when
    Optional<Message> result = messageRepository.findTopByChannelIdOrderByCreatedAtDesc(
        channel.getId());

    // then
    assertThat(result).isEmpty();
  }

  // ───── findAllByChannelIdAndCreatedAtBefore ─────

  @Test
  void findAllByChannelIdAndCreatedAtBefore_커서이전메시지반환() {
    // given
    Instant past = Instant.now().minusSeconds(100);
    Instant future = Instant.now().plusSeconds(100);

    messageRepository.saveAndFlush(new Message("첫번째 메시지", channel, author));

    // when - 미래 시간 기준으로 이전 메시지 조회
    Slice<Message> result = messageRepository
        .findAllByChannelIdAndCreatedAtBeforeOrderByCreatedAtDesc(
            channel.getId(), future, PageRequest.of(0, 10));

    // then
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getContent()).isEqualTo("첫번째 메시지");
  }

  @Test
  void findAllByChannelIdAndCreatedAtBefore_빈목록() {
    // when
    Slice<Message> result = messageRepository
        .findAllByChannelIdAndCreatedAtBeforeOrderByCreatedAtDesc(
            channel.getId(), Instant.now(), PageRequest.of(0, 10));

    // then
    assertThat(result.getContent()).isEmpty();
  }
}