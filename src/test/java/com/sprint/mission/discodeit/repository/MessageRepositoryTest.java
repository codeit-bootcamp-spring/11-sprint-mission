package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.config.TestJpaConfig;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestJpaConfig.class)
class MessageRepositoryTest {

  @Autowired
  private MessageRepository messageRepository;
  @Autowired
  private ChannelRepository channelRepository;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private UserStatusRepository userStatusRepository;

  private Channel channel;
  private User author;

  @BeforeEach
  void setUp() {
    channel = channelRepository.save(new Channel(ChannelType.PUBLIC, "general", null));
    author = userRepository.save(new User("testuser", "test@example.com", "pass", null));
    userStatusRepository.save(new UserStatus(author, Instant.now()));
  }

  @Test
  void findAllByChannelIdWithAuthor_succeeds() {
    messageRepository.save(new Message("첫 번째 메시지", channel, author, List.of()));
    messageRepository.save(new Message("두 번째 메시지", channel, author, List.of()));

    PageRequest pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
    Slice<Message> result = messageRepository.findAllByChannelIdWithAuthor(
        channel.getId(), Instant.now().plusSeconds(60), pageable);

    assertThat(result.getContent()).hasSize(2);
    assertThat(result.getContent()).extracting(Message::getContent)
        .containsExactlyInAnyOrder("첫 번째 메시지", "두 번째 메시지");
  }

  @Test
  void findAllByChannelIdWithAuthor_whenNoMessages_returnsEmptySlice() {
    Channel emptyChannel = channelRepository.save(new Channel(ChannelType.PUBLIC, "empty", null));

    PageRequest pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
    Slice<Message> result = messageRepository.findAllByChannelIdWithAuthor(
        emptyChannel.getId(), Instant.now().plusSeconds(60), pageable);

    assertThat(result.getContent()).isEmpty();
    assertThat(result.hasNext()).isFalse();
  }

  @Test
  void findAllByChannelIdWithAuthor_excludesMessagesAfterCursor() {
    messageRepository.save(new Message("과거 메시지", channel, author, List.of()));

    PageRequest pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
    Slice<Message> result = messageRepository.findAllByChannelIdWithAuthor(
        channel.getId(), Instant.now().minusSeconds(60), pageable);

    assertThat(result.getContent()).isEmpty();
  }

  @Test
  void findAllByChannelIdWithAuthor_whenMoreThanPageSize_hasNextIsTrue() {
    messageRepository.save(new Message("메시지1", channel, author, List.of()));
    messageRepository.save(new Message("메시지2", channel, author, List.of()));
    messageRepository.save(new Message("메시지3", channel, author, List.of()));

    PageRequest pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "createdAt"));
    Slice<Message> result = messageRepository.findAllByChannelIdWithAuthor(
        channel.getId(), Instant.now().plusSeconds(60), pageable);

    assertThat(result.getContent()).hasSize(2);
    assertThat(result.hasNext()).isTrue();
  }

  @Test
  void findLastMessageAtByChannelId_succeeds() {
    messageRepository.save(new Message("메시지1", channel, author, List.of()));
    messageRepository.save(new Message("메시지2", channel, author, List.of()));

    Optional<Instant> result = messageRepository.findLastMessageAtByChannelId(channel.getId());

    assertThat(result).isPresent();
  }

  @Test
  void findLastMessageAtByChannelId_whenNoMessages_returnsEmpty() {
    Channel emptyChannel = channelRepository.save(new Channel(ChannelType.PUBLIC, "empty", null));

    Optional<Instant> result = messageRepository.findLastMessageAtByChannelId(emptyChannel.getId());

    assertThat(result).isEmpty();
  }

  @Test
  void deleteAllByChannelId_succeeds() {
    messageRepository.save(new Message("메시지1", channel, author, List.of()));
    messageRepository.save(new Message("메시지2", channel, author, List.of()));

    messageRepository.deleteAllByChannelId(channel.getId());

    PageRequest pageable = PageRequest.of(0, 10);
    Slice<Message> result = messageRepository.findAllByChannelIdWithAuthor(
        channel.getId(), Instant.now().plusSeconds(60), pageable);
    assertThat(result.getContent()).isEmpty();
  }

  @Test
  void deleteAllByChannelId_doesNotDeleteOtherChannelMessages() {
    Channel otherChannel = channelRepository.save(new Channel(ChannelType.PUBLIC, "other", null));
    messageRepository.save(new Message("삭제 대상", channel, author, List.of()));
    messageRepository.save(new Message("유지 대상", otherChannel, author, List.of()));

    messageRepository.deleteAllByChannelId(channel.getId());

    long remaining = messageRepository.count();
    assertThat(remaining).isEqualTo(1);

    PageRequest pageable = PageRequest.of(0, 10);
    Slice<Message> otherMessages = messageRepository.findAllByChannelIdWithAuthor(
        otherChannel.getId(), Instant.now().plusSeconds(60), pageable);
    assertThat(otherMessages.getContent()).hasSize(1);
    assertThat(otherMessages.getContent().get(0).getContent()).isEqualTo("유지 대상");
  }
}