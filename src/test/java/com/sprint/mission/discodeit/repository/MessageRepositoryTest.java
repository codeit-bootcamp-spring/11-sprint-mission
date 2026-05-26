package com.sprint.mission.discodeit.repository;


import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Channel.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EnableJpaAuditing
@Transactional
public class MessageRepositoryTest {

  @Autowired
  JPAMessageRepository messageRepository;

  @Autowired
  JPAChannelRepository channelRepository;

  @Autowired
  JPAUserRepository userRepository;


  @Test
  @DisplayName("커서 있는 채널 아이디로 메시지 가져오기")
  void findAllByChannelIdCursorTest() throws InterruptedException {

    //given

    Channel channel = new Channel("channel", "channel description", ChannelType.PUBLIC);
    User user = new User("user", "test@test.com", "password1", null, null);

    Message message1 = new Message(user, channel, "message1", null);
    Message message2 = new Message(user, channel, "message2", null);

    channelRepository.save(channel);
    userRepository.save(user);

    Thread.sleep(50);
    messageRepository.save(message1);
    Thread.sleep(50);
    Instant timing = Instant.now();
    messageRepository.save(message2);

    Pageable pageable = PageRequest.of(0, 10, Direction.DESC, "createdAt");

    //when
    Slice<Message> messages = messageRepository.findAllByChannel_Id(channel.getId(), pageable,
        Instant.now());
    Slice<Message> messages2 = messageRepository.findAllByChannel_Id(channel.getId(), pageable,
        timing);

    //then
    assertThat(messages).isNotNull();
    assertThat(messages.getNumberOfElements()).isEqualTo(2);

    assertThat(messages2).isNotNull();
    assertThat(messages2.getNumberOfElements()).isEqualTo(1);


  }


}
