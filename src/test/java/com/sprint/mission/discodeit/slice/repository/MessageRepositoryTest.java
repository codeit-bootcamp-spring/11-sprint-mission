package com.sprint.mission.discodeit.slice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@EnableJpaAuditing
public class MessageRepositoryTest {

  @Autowired
  private MessageRepository messageRepository;

  @Autowired
  private ChannelRepository channelRepository;

  @Autowired
  private UserRepository userRepository;

  @Test
  @DisplayName("채널 메시지 조회 성공")
  void findMessages_success() {
    // given
    User user = userRepository.save(User.create("test", "test@naver.com", "12345678"));
    Channel channel = channelRepository.save(Channel.createPublic("공개", "공개 채널입니다."));

    Message message1 = messageRepository.save(Message.create("메시지1", channel, user));
    Message message2 = messageRepository.save(Message.create("메시지2", channel, user));

    Pageable pageable = PageRequest.of(0, 50);

    // when
    Slice<Message> result = messageRepository.findMessages(
        channel.getId(),
        message2.getCreatedAt().plusMillis(3000),
        pageable);

    // then
    assertThat(result.getContent()).hasSize(2);
  }

  @Test
  @DisplayName("채널 메시지 조회 실패(채널에 메시지가 없음)")
  void findMessages_fail_emptyMessages() {
    // given
    Channel channel = Channel.createPublic("공개", "공개 채널입니다.");
    Pageable pageable = PageRequest.of(0, 50);

    // when
    Slice<Message> result = messageRepository.findMessages(
        channel.getId(),
        Instant.now().plusMillis(3000),
        pageable);

    // then
    assertThat(result.getContent()).isEmpty();
  }

  @Test
  @DisplayName("채널들의 마지막 메시지 조회 성공")
  void findLastMessages_success() throws InterruptedException {
    // given
    User user = userRepository.save(User.create("test", "test@naver.com", "12345678"));

    Channel channel1 = channelRepository.save(Channel.createPublic("채널1", "첫 번째 채널입니다."));
    Channel channel2 = channelRepository.save(Channel.createPublic("채널2", "두 번째 채널입니다."));

    // message 1,2는 channel1 / message 3은 channel2에
    Message message1 = messageRepository.save(Message.create("메시지1", channel1, user));
    Thread.sleep(1); // 메시지 생성 시간이 같음을 방지(간혹 실패.. 방지)

    // 채널1 마지막 메시지
    Message message2 = messageRepository.save(Message.create("메시지2", channel1, user));
    Thread.sleep(1); // 메시지 생성 시간이 같음을 방지(간혹 실패.. 방지)

    // 채널2 마지막 메시지
    Message message3 = messageRepository.save(Message.create("메시지3", channel2, user));

    // when
    List<Message> result = messageRepository.findLastMessagesByChannelIds(
        List.of(channel1.getId(), channel2.getId()));

    // then : 마지막 메시지가 message2, message3인지 확인
    assertThat(result).containsExactlyInAnyOrder(message2, message3);
  }

  @Test
  @DisplayName("채널들의 마지막 메시지 조회 실패(채널에 메시지가 없음)")
  void findLastMessages_fail_emptyMessages() {
    // given : 메시지 생성X
    Channel channel = channelRepository.save(Channel.createPublic("공개", "공개 채널입니다."));

    // when
    List<Message> result = messageRepository.findLastMessagesByChannelIds(List.of(channel.getId()));

    // then
    assertThat(result).isEmpty();

  }

  @Test
  @DisplayName("채널의 마지막 메시지 조회 성공")
  void findTopByChannelIdOrderByCreatedAtDesc_success() throws InterruptedException {
    // given
    User user = userRepository.save(User.create("test", "test@naver.com", "12345678"));
    Channel channel = channelRepository.save(Channel.createPublic("공개", "공개 채널입니다."));

    Message firstMessage = messageRepository.save(Message.create("첫 메시지", channel, user));
    Thread.sleep(1); // 메시지 생성 시간이 같음을 방지(간혹 실패.. 방지)
    Message lastMessage = messageRepository.save(Message.create("마지막 메시지", channel, user));

    // when
    Optional<Message> result = messageRepository.findTopByChannelIdOrderByCreatedAtDesc(
        channel.getId());

    // then : 존재하는지, 마지막 메시지가 lastMessage인지
    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(lastMessage);
  }

  @Test
  @DisplayName("채널의 마지막 메시지 조회 실패(채널에 메시지가 없음)")
  void findTopByChannelIdOrderByCreatedAtDesc_fail_emptyMessages() {
    // given
    Channel channel = channelRepository.save(Channel.createPublic("공개", "공개 채널입니다."));

    // when
    Optional<Message> result = messageRepository.findTopByChannelIdOrderByCreatedAtDesc(
        channel.getId());

    // then
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("채널의 모든 메시지 삭제 성공")
  void deleteAllByChannelId_success() {
    // given
    User user = userRepository.save(User.create("test", "test@naver.com", "12345678"));
    Channel channel = channelRepository.save(Channel.createPublic("공개", "공개 채널입니다."));

    Message message1 = messageRepository.save(Message.create("메시지1", channel, user));
    Message message2 = messageRepository.save(Message.create("메시지2", channel, user));

    // when
    messageRepository.deleteAllByChannelId(channel.getId());

    // then : 메시지가 비어있어야 함
    Slice<Message> result = messageRepository.findMessages(
        channel.getId(),
        message2.getCreatedAt().plusMillis(3000),
        PageRequest.of(0, 50)
    );

    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("채널의 모든 메시지 삭제 실패(채널이 존재하지 않음)")
  void deleteAllByChannelId_fail_notfound_channel() {
    // given
    User user = userRepository.save(User.create("test", "test@naver.com", "12345678"));
    Channel channel = channelRepository.save(Channel.createPublic("공개", "공개 채널입니다."));
    UUID otherChannelId = UUID.randomUUID();

    Message message = messageRepository.save(Message.create("메시지", channel, user));

    // when
    messageRepository.deleteAllByChannelId(otherChannelId);

    // then : 채널의 메시지가 삭제되지 않고 그대로 1개 유지
    Slice<Message> result = messageRepository.findMessages(
        channel.getId(),
        message.getCreatedAt().plusMillis(3000),
        PageRequest.of(0, 50)
    );

    assertThat(result).hasSize(1);
  }

}
