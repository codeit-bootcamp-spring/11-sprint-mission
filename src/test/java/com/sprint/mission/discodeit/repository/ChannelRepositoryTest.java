package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.config.TestJpaConfig;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestJpaConfig.class)
class ChannelRepositoryTest {

  @Autowired
  private ChannelRepository channelRepository;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private UserStatusRepository userStatusRepository;
  @Autowired
  private ReadStatusRepository readStatusRepository;

  private Channel publicChannel;
  private Channel privateChannel;
  private User user;

  @BeforeEach
  void setUp() {
    publicChannel = channelRepository.save(new Channel(ChannelType.PUBLIC, "general", "공개 채널"));
    privateChannel = channelRepository.save(new Channel(ChannelType.PRIVATE, null, null));

    user = userRepository.save(new User("testuser", "test@example.com", "pass", null));
    userStatusRepository.save(new UserStatus(user, Instant.now()));
  }

  @Test
  void findAllByTypeOrIdIn_byPublicType_succeeds() {
    List<Channel> result = channelRepository.findAllByTypeOrIdIn(ChannelType.PUBLIC, List.of(UUID.randomUUID()));

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getType()).isEqualTo(ChannelType.PUBLIC);
    assertThat(result.get(0).getName()).isEqualTo("general");
  }

  @Test
  void findAllByTypeOrIdIn_excludesPrivateChannelsByType() {
    List<Channel> result = channelRepository.findAllByTypeOrIdIn(ChannelType.PUBLIC, List.of(UUID.randomUUID()));

    boolean hasPrivate = result.stream().anyMatch(c -> c.getType() == ChannelType.PRIVATE);
    assertThat(hasPrivate).isFalse();
  }

  @Test
  void findAllByTypeOrIdIn_byPrivateChannelId_succeeds() {
    List<Channel> result = channelRepository.findAllByTypeOrIdIn(ChannelType.PUBLIC, List.of(privateChannel.getId()));

    assertThat(result).hasSize(2);
    assertThat(result).extracting(Channel::getId)
        .containsExactlyInAnyOrder(publicChannel.getId(), privateChannel.getId());
  }

  @Test
  void findAllByTypeOrIdIn_whenNoSubscribedChannels_returnsOnlyPublic() {
    List<Channel> result = channelRepository.findAllByTypeOrIdIn(ChannelType.PUBLIC, List.of(UUID.randomUUID()));

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getType()).isEqualTo(ChannelType.PUBLIC);
  }

  @Test
  void findAllByTypeOrIdIn_whenNoPublicChannels_returnsOnlyPrivate() {
    channelRepository.delete(publicChannel);

    List<Channel> result = channelRepository.findAllByTypeOrIdIn(ChannelType.PUBLIC, List.of(privateChannel.getId()));

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getType()).isEqualTo(ChannelType.PRIVATE);
  }

  @Test
  void findAllByTypeOrIdIn_includesSubscribedPrivateChannels() {
    ReadStatus readStatus = new ReadStatus(user, privateChannel, Instant.now());
    readStatusRepository.save(readStatus);

    List<UUID> subscribedChannelIds = readStatusRepository.findAllByUserId(user.getId()).stream()
        .map(rs -> rs.getChannel().getId())
        .toList();

    List<Channel> result = channelRepository.findAllByTypeOrIdIn(ChannelType.PUBLIC, subscribedChannelIds);

    assertThat(result).hasSize(2);
    assertThat(result).extracting(Channel::getId)
        .containsExactlyInAnyOrder(publicChannel.getId(), privateChannel.getId());
  }

  @Test
  void findAllByTypeOrIdIn_whenUserHasNoSubscriptions_returnsOnlyPublic() {
    List<UUID> subscribedChannelIds = readStatusRepository.findAllByUserId(user.getId()).stream()
        .map(rs -> rs.getChannel().getId())
        .toList();

    List<Channel> result = channelRepository.findAllByTypeOrIdIn(ChannelType.PUBLIC, subscribedChannelIds);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getType()).isEqualTo(ChannelType.PUBLIC);
  }
}