package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.config.JpaConfig;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaConfig.class)
class MessageRepositoryTest {

  @Autowired
  private MessageRepository messageRepository;

  @Autowired
  private TestEntityManager entityManager;

  private User createTestUser(String username, String email) {
    BinaryContent profile = BinaryContent.builder()
        .fileName("profile.jpg")
        .size(1024L)
        .contentType("image/jpeg")
        .build();
    entityManager.persist(profile);

    User user = User.builder()
        .username(username)
        .email(email)
        .password("password123!")
        .profile(profile)
        .build();
    entityManager.persist(user);

    UserStatus status = UserStatus.builder()
        .user(user)
        .lastActiveAt(Instant.now())
        .build();
    entityManager.persist(status);

    return user;
  }

  private Channel createTestChannel(ChannelType type, String name) {
    Channel channel = Channel.builder()
        .type(type)
        .name(name)
        .description("설명: " + name)
        .build();
    entityManager.persist(channel);
    return channel;
  }

  private Message createTestMessage(String content, Channel channel, User author,
      Instant createdAt) {
    Message message = Message.builder()
        .content(content)
        .channel(channel)
        .author(author)
        .attachments(new ArrayList<>())
        .build();
    entityManager.persist(message);
    entityManager.flush();

    if (createdAt != null) {
      entityManager.getEntityManager()
          .createNativeQuery("UPDATE messages SET created_at = ?1 WHERE id = ?2")
          .setParameter(1, createdAt)
          .setParameter(2, message.getId())
          .executeUpdate();
      entityManager.clear();
      return entityManager.find(Message.class, message.getId());
    }
    return message;
  }

  // findAllByChannelId 테스트
  @Test
  @DisplayName("채널 ID와 생성 시간으로 메시지 페이징 조회 - 성공")
  void findAllByChannelId_pagination_success() {
    // Given
    User user = createTestUser("testUser", "test@example.com");
    Channel channel = createTestChannel(ChannelType.PUBLIC, "테스트채널");

    Instant now = Instant.now();
    Instant fiveMinutesAgo = now.minus(5, ChronoUnit.MINUTES);
    Instant tenMinutesAgo = now.minus(10, ChronoUnit.MINUTES);

    createTestMessage("첫 번째 메시지", channel, user, tenMinutesAgo);
    createTestMessage("두 번째 메시지", channel, user, fiveMinutesAgo);
    createTestMessage("세 번째 메시지", channel, user, now);

    entityManager.flush();
    entityManager.clear();

    // When
    Slice<Message> messages = messageRepository.findAllByChannelId(
        channel.getId(),
        now.plus(1, ChronoUnit.MINUTES),
        PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "createdAt"))
    );

    // Then
    assertThat(messages).isNotNull();
    assertThat(messages.hasContent()).isTrue();
    assertThat(messages.getNumberOfElements()).isEqualTo(2);
    assertThat(messages.hasNext()).isTrue();

    List<Message> content = messages.getContent();
    assertThat(content.get(0).getContent()).isEqualTo("세 번째 메시지");
    assertThat(content.get(1).getContent()).isEqualTo("두 번째 메시지");
    assertThat(content.get(0).getCreatedAt()).isAfterOrEqualTo(content.get(1).getCreatedAt());

    Message firstMessage = content.get(0);
    assertThat(Hibernate.isInitialized(firstMessage.getAuthor())).isTrue();
  }

  // findTopByChannelIdOrderByCreatedAtDesc 테스트
  @Test
  @DisplayName("채널 내 가장 최근에 생성된 메시지 조회 - 성공")
  void findTopByChannelIdOrderByCreatedAtDesc_success() {
    // Given
    User user = createTestUser("testUser", "test@example.com");
    Channel channel = createTestChannel(ChannelType.PUBLIC, "테스트채널");

    Instant now = Instant.now();
    Instant fiveMinutesAgo = now.minus(5, ChronoUnit.MINUTES);
    Instant tenMinutesAgo = now.minus(10, ChronoUnit.MINUTES);

    createTestMessage("첫 번째 메시지", channel, user, tenMinutesAgo);
    createTestMessage("두 번째 메시지", channel, user, fiveMinutesAgo);
    Message lastMessage = createTestMessage("세 번째 메시지", channel, user, now);

    entityManager.flush();
    entityManager.clear();

    // When
    Optional<Message> lastMessageAt = messageRepository.findTopByChannelIdOrderByCreatedAtDesc(
        channel.getId());

    // Then
    assertThat(lastMessageAt).isPresent();
    assertThat(lastMessageAt.get().getContent()).isEqualTo("세 번째 메시지");
    assertThat(lastMessageAt.get().getCreatedAt().truncatedTo(ChronoUnit.MILLIS))
        .isEqualTo(lastMessage.getCreatedAt().truncatedTo(ChronoUnit.MILLIS));
  }

  @Test
  @DisplayName("메시지가 없는 채널의 최근 메시지 조회 - 빈 Optional 반환")
  void findTopByChannelIdOrderByCreatedAtDesc_noMessages_returnsEmpty() {
    // Given
    Channel emptyChannel = createTestChannel(ChannelType.PUBLIC, "빈채널");

    entityManager.flush();
    entityManager.clear();

    // When
    Optional<Message> lastMessageAt = messageRepository.findTopByChannelIdOrderByCreatedAtDesc(
        emptyChannel.getId());

    // Then
    assertThat(lastMessageAt).isEmpty();
  }

  // deleteByChannelId 테스트
  @Test
  @DisplayName("채널 ID로 소속된 모든 메시지 삭제 - 성공")
  void deleteByChannelId_success() {
    // Given
    User user = createTestUser("testUser", "test@example.com");
    Channel channel = createTestChannel(ChannelType.PUBLIC, "테스트채널");
    Channel otherChannel = createTestChannel(ChannelType.PUBLIC, "다른채널");

    createTestMessage("첫 번째 메시지", channel, user, null);
    createTestMessage("두 번째 메시지", channel, user, null);
    createTestMessage("세 번째 메시지", channel, user, null);
    createTestMessage("다른 채널 메시지", otherChannel, user, null);

    entityManager.flush();
    entityManager.clear();

    // When
    messageRepository.deleteByChannelId(channel.getId());
    entityManager.flush();
    entityManager.clear();

    // Then
    List<Message> channelMessages = messageRepository.findAllByChannelId(
        channel.getId(),
        Instant.now().plus(1, ChronoUnit.DAYS),
        PageRequest.of(0, 100)
    ).getContent();
    assertThat(channelMessages).isEmpty();

    List<Message> otherChannelMessages = messageRepository.findAllByChannelId(
        otherChannel.getId(),
        Instant.now().plus(1, ChronoUnit.DAYS),
        PageRequest.of(0, 100)
    ).getContent();
    assertThat(otherChannelMessages).hasSize(1);
  }
}