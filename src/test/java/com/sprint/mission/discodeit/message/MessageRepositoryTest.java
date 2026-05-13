package com.sprint.mission.discodeit.message;


import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
public class MessageRepositoryTest {

  @Autowired
  ChannelRepository channelRepository;

  @Autowired
  UserRepository userRepository;

  @Autowired
  private MessageRepository messageRepository;

  @Test
  @DisplayName("특정 채널의 가장 최근 메시지 1건 조회 성공")
  void findFirstByChannelIdOrderByCreatedAtDesc_success() throws InterruptedException {
    //given
    User user = new User("마크", "mark@test.com", "pass1234", null);
    userRepository.save(user);
    Channel channel = new Channel("채팅방", "채팅방", ChannelType.PUBLIC);
    channelRepository.save(channel);

    Message message1 = new Message(user, channel, "과거 메시지", null);
    messageRepository.save(message1);
    Thread.sleep(10); // JPA Auditing 시간이 똑같이 찍히는 것을 방지하기 위한 미세 지연
    Message message2 = new Message(user, channel, "최근 메시지", null);
    messageRepository.save(message2);
    //when
    Optional<Message> result = messageRepository.findFirstByChannelIdOrderByCreatedAtDesc(
        channel.getId());

    //then
    assertThat(result).isPresent();
    assertThat(result.get().getContent()).isEqualTo("최근 메시지");
  }

  @Test
  @DisplayName("채널에 메시지가 없을 경우 빈 Optional 반환")
  void findFirstByChannelIdOrderByCreatedAtDesc_empty() {
    Channel channel = new Channel("채팅방", "채팅방", ChannelType.PUBLIC);
    channelRepository.save(channel);

    Optional<Message> result = messageRepository.findFirstByChannelIdOrderByCreatedAtDesc(
        channel.getId());

    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("특정 채널의 메시지를 최신순으로 페이징 조회 성공")
  void findByChannelIdOrderByCreatedAtDesc_success() {
    User user = new User("마크", "mark@test.com", "pass1234", null);
    userRepository.save(user);
    Channel channel = new Channel("채팅방", "채팅방", ChannelType.PUBLIC);
    channelRepository.save(channel);
    Pageable pageable = PageRequest.of(0, 50);

    Message message1 = new Message(user, channel, "과거 메시지", null);
    messageRepository.save(message1);
    Message message2 = new Message(user, channel, "최근 메시지", null);
    messageRepository.save(message2);

    Slice<Message> result = messageRepository.findByChannelIdOrderByCreatedAtDesc(channel.getId(),
        pageable);

    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(2);
  }

  @Test
  @DisplayName("채널의 메시지를 페이징 조회 시 데이터가 없으면 빈 Slice 반환")
  void findByChannelIdOrderByCreatedAtDesc_empty() {
    Channel channel = new Channel("채팅방", "채팅방", ChannelType.PUBLIC);
    channelRepository.save(channel);
    Pageable pageable = PageRequest.of(0, 50);

    Slice<Message> result = messageRepository.findByChannelIdOrderByCreatedAtDesc(channel.getId(),
        pageable);

    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("특정 커서(시간) 이전의 메사지를 최신순으로 조회 성공 (커서 기반 페이지네이션)")
  void findByChannelIdAndCreatedAtLessThanOrderByCreatedAtDesc_success() {
    //given
    User user = new User("마크", "mark@test.com", "pass1234", null);
    userRepository.save(user);
    Channel channel = new Channel("채팅방", "채팅방", ChannelType.PUBLIC);
    channelRepository.save(channel);
    Pageable pageable = PageRequest.of(0, 50);

    Message message1 = new Message(user, channel, "과거 메시지", null);
    messageRepository.save(message1);
    Message message2 = new Message(user, channel, "최근 메시지", null);
    message2 = messageRepository.save(message2); // 커서에 시간을 할당하기 위해 원본 객체에 재할당 필수! (아니면 null로 찍힘)

    Instant cursor = message2.getCreatedAt();

    //when
    Slice<Message> result = messageRepository.findByChannelIdAndCreatedAtLessThanOrderByCreatedAtDesc(
        channel.getId(), cursor, pageable);

    //then
    assertThat(cursor).isNotNull();
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1); // 커서(최근 메시지)보다 작은(>) 과거 데이터만 조회되어야 함.
    assertThat(result.getContent().get(0).getContent()).isEqualTo("과거 메시지");
  }

  @Test
  @DisplayName("특정 시간 이전의 메시지를 조회할 때 이전 데이터가 없으면 빈 Slice 반환")
  void findByChannelIdAndCreatedAtLessThanOrderByCreatedAtDesc_empty() {
    //given
    Channel channel = new Channel("채팅방", "채팅방", ChannelType.PUBLIC);
    channelRepository.save(channel);
    Instant cursor = Instant.now();
    Pageable pageable = PageRequest.of(0, 50);

    //when
    Slice<Message> result = messageRepository.findByChannelIdAndCreatedAtLessThanOrderByCreatedAtDesc(
        channel.getId(), cursor, pageable);

    //then
    assertThat(result).isEmpty();
  }
}
