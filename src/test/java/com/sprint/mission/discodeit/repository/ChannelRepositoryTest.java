package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Channel.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EnableJpaAuditing
@Transactional
public class ChannelRepositoryTest {

  @Autowired
  private JPAChannelRepository channelRepository;

  @Autowired
  private JPAUserRepository userRepository;

  @Autowired
  private JPAReadStatusRepository readStatusRepository;


  @BeforeEach
  void setUp() {

  }


  @Test
  @DisplayName("유저가 속한 채널 찾기")
  void findAllChannelByUserIdTest() {

    //given
    Channel channel1 = new Channel(
        "testchannel1",
        "채널1",
        ChannelType.PUBLIC

    );
    Channel channel2 = new Channel(
        null,
        null,
        ChannelType.PRIVATE

    );

    channelRepository.save(channel1);
    channelRepository.save(channel2);
    User user = new User("유저", "test@test.com", "password1", null, null);

    userRepository.save(user);

    readStatusRepository.save(new ReadStatus(user, channel1, Instant.now()));
    readStatusRepository.save(new ReadStatus(user, channel2, Instant.now()));

    // when
    List<Channel> result = channelRepository.findAllByUser_Id(user.getId());

    assertThat(result).isNotNull();
    assertThat(result).hasSize(2);
    assertThat(result).filteredOn(channel -> channel.getType().equals(ChannelType.PUBLIC))
        .hasSize(1);
    assertThat(result).filteredOn(channel -> channel.getType().equals(ChannelType.PRIVATE))
        .hasSize(1);


  }


  @Test
  @DisplayName("채널에 속하지 않은 유저가 속한 채널 찾기")
  void findAllChannelByDifferentUserIdTest() {

    //given
    Channel channel1 = new Channel(
        "testchannel1",
        "채널1",
        ChannelType.PUBLIC

    );
    Channel channel2 = new Channel(
        null,
        null,
        ChannelType.PRIVATE

    );

    channelRepository.save(channel1);
    channelRepository.save(channel2);
    User user = new User("유저", "test@test.com", "password1", null, null);

    userRepository.save(user);

    // when
    List<Channel> result = channelRepository.findAllByUser_Id(user.getId());

    assertThat(result).isNotNull();
    assertThat(result).hasSize(0);


  }


}
