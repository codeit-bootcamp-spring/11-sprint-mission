package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
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
  private UserRepository userRepository;

  @Autowired
  private ChannelRepository channelRepository;

  private User testUser;
  private Channel testChannel;

  @BeforeEach
  void setUp() {
    testUser = userRepository.save(new User("tester", "test@mail.com", "pw"));
    testChannel = channelRepository.save(new Channel(ChannelType.PUBLIC, "테스트채널", "설명"));
  }

  @Test
  @DisplayName("채널 ID로 메시지 목록 페이징 조회 - 메시지가 있는 경우")
  void findByChannelId_success() {
    messageRepository.save(new Message("첫번째", testUser, testChannel));
    messageRepository.save(new Message("두번째", testUser, testChannel));

    PageRequest pageRequest = PageRequest.of(0, 10);

    Slice<Message> result = messageRepository.findByChannelIdOrderByCreatedAtDesc(
        testChannel.getId(), pageRequest);

    assertThat(result.getContent()).hasSize(2);
  }

  @Test
  @DisplayName("채널 ID로 메시지 목록 페이징 조회 - 메시지가 없는 빈 채널인 경우")
  void findByChannelId_empty() {
    PageRequest pageRequest = PageRequest.of(0, 10);

    Slice<Message> result = messageRepository.findByChannelIdOrderByCreatedAtDesc(
        testChannel.getId(), pageRequest);

    assertThat(result.getContent()).isEmpty();
    assertThat(result.hasNext()).isFalse();
  }

  @Test
  @DisplayName("채널의 가장 최신 메시지 1건 조회 - 존재하는 경우")
  void findTopByChannel_found() {
    messageRepository.save(new Message("오래된 메시지", testUser, testChannel));
    messageRepository.save(new Message("가장 최근 메시지", testUser, testChannel));

    Optional<Message> topMessage = messageRepository.findTopByChannelOrderByCreatedAtDesc(
        testChannel);

    assertThat(topMessage).isPresent();
    assertThat(topMessage.get().getContent()).isEqualTo("가장 최근 메시지");
  }

  @Test
  @DisplayName("채널의 가장 최신 메시지 1건 조회 - 메시지가 없는 경우")
  void findTopByChannel_notFound() {
    Optional<Message> topMessage = messageRepository.findTopByChannelOrderByCreatedAtDesc(
        testChannel);

    assertThat(topMessage).isEmpty();
  }
}
